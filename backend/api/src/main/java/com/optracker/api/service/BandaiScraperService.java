package com.optracker.api.service;

import com.optracker.api.entity.Card;
import com.optracker.api.entity.CardSet;
import com.optracker.api.entity.CardVariant;
import com.optracker.api.repository.CardRepository;
import com.optracker.api.repository.CardSetRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BandaiScraperService {

    private static final String BASE_URL = "https://en.onepiece-cardgame.com/cardlist/";
    private final CardRepository cardRepository;
    private final CardSetRepository cardSetRepository;

    public BandaiScraperService(CardRepository cardRepository, CardSetRepository cardSetRepository) {
        this.cardRepository = cardRepository;
        this.cardSetRepository = cardSetRepository;
    }

    private static class SetInfo {
        String url;
        String name;
        SetInfo(String url, String name) { this.url = url; this.name = name; }
    }

    public boolean hasData() {
        return cardSetRepository.count() > 0;
    }

    @Async
    public void scrapeOfficialSite() {
        System.out.println("\n🤖 [SISTEMA] A mapear todos os Sets da Bandai...");

        Map<String, SetInfo> allSets = discoverAllSets();

        if (allSets.isEmpty()) {
            System.out.println("⚠️ [INFO] Nenhum Set encontrado para processar.");
            return;
        }

        // 🔍 FASE 1: Descobrir o estado de cada Set (Quais estão prontos vs quais faltam)
        List<String> completedSets = new ArrayList<>();
        Map<String, SetInfo> setsNeedingUpdate = new LinkedHashMap<>();

        for (Map.Entry<String, SetInfo> entry : allSets.entrySet()) {
            String setIdKey = entry.getKey();
            SetInfo info = entry.getValue();

            Optional<CardSet> currentSetOpt = cardSetRepository.findBySetId(setIdKey);
            if (currentSetOpt.isPresent()) {
                CardSet currentSet = currentSetOpt.get();
                if (currentSet.getTotalCards() != null && currentSet.getTotalCards() > 0) {
                    completedSets.add(info.name + " (" + setIdKey + ")");
                    continue;
                }
            }
            setsNeedingUpdate.put(setIdKey, info);
        }

        // 📊 RELATÓRIO INICIAL DE IMPACTO
        System.out.println("=================================================");
        System.out.println("📊 STATUS DA BASE DE DADOS:");
        System.out.println("✅ Sets já atualizados: " + completedSets.size() + "/" + allSets.size());
        System.out.println("⏳ Sets que necessitam de extração/atualização: " + setsNeedingUpdate.size());
        System.out.println("=================================================");

        if (setsNeedingUpdate.isEmpty()) {
            System.out.println("✨ Todos os Sets já estão 100% sincronizados!");
            return;
        }

        System.out.println("\n🚀 A INICIAR EXTRAÇÃO DOS SETS PENDENTES:");
        for (String setName : setsNeedingUpdate.values().stream().map(s -> s.name).toList()) {
            System.out.println("   📌 Pendente: " + setName);
        }
        System.out.println("-------------------------------------------------\n");

        // 🔄 FASE 2: Processar cada Set numa transação INDEPENDENTE
        int currentSetIndex = 1;
        int totalNewOrUpdatedCards = 0;

        for (Map.Entry<String, SetInfo> entry : setsNeedingUpdate.entrySet()) {
            String setIdKey = entry.getKey();
            SetInfo info = entry.getValue();

            try {
                System.out.println("⬇️ [" + currentSetIndex + "/" + setsNeedingUpdate.size() + "] A extrair Set: " + info.name + " (" + setIdKey + ")...");

                // Executa a transação isolada para este Set
                int cardsAdded = processAndSaveSingleSet(setIdKey, info);
                totalNewOrUpdatedCards += cardsAdded;

                System.out.println("✅ [" + currentSetIndex + "/" + setsNeedingUpdate.size() + "] Concluído: " + info.name);
                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println("🚨 [ERRO] Falha no Set " + info.name + ": " + e.getMessage());
            } finally {
                currentSetIndex++;
            }
        }

        System.out.println("\n🎉 Extração concluída! Novas cartas/artes adicionadas: " + totalNewOrUpdatedCards + "\n");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int processAndSaveSingleSet(String setIdKey, SetInfo info) throws Exception {
        int cardsAddedInThisSet = 0;

        // 1. Procurar ou criar o CardSet garantindo o vínculo com o ID existente
        CardSet currentSet = cardSetRepository.findBySetId(setIdKey).orElseGet(() -> {
            CardSet newSet = new CardSet();
            newSet.setSetId(setIdKey);
            return newSet;
        });

        currentSet.setName(info.name);
        currentSet = cardSetRepository.save(currentSet); // Guardar e atualizar a referência com ID

        Document doc = Jsoup.connect(info.url).userAgent("Mozilla/5.0").timeout(25000).get();
        Elements cardElements = doc.select(".resultCol .modalCol");

        if (cardElements.isEmpty()) {
            return 0;
        }

        int cardsInThisSet = cardElements.size();
        Map<String, Card> setCardMap = new HashMap<>();

        for (Element cardHtml : cardElements) {
            Elements infoSpans = cardHtml.select(".infoCol span");
            if (infoSpans.isEmpty()) continue;

            String code = infoSpans.get(0).text().trim();
            if (code.isEmpty()) continue;

            Element imgElement = cardHtml.selectFirst(".imgCol img");
            if (imgElement == null) imgElement = cardHtml.selectFirst(".frontCol img");

            String imgSrc = "";
            if (imgElement != null) {
                imgSrc = imgElement.hasAttr("data-src") ? imgElement.absUrl("data-src") : imgElement.absUrl("src");
            }

            if (imgSrc.isEmpty()) continue;

            // 2. Procurar no Map local ou na Base de Dados pelo Código da carta (Evita o Duplicate Entry no Code)
            Card card = setCardMap.get(code);
            if (card == null) {
                Optional<Card> existingCardOpt = cardRepository.findByCodeWithVariants(code);
                if (existingCardOpt.isPresent()) {
                    card = existingCardOpt.get(); // Usa a entidade existente com ID
                } else {
                    card = new Card();
                    card.setCode(code); // Nova carta
                }

                if (card.getName() == null || card.getEffect() == null || card.getCost() == null) {
                    card.setRarity(infoSpans.size() >= 2 ? infoSpans.get(1).text().trim() : "");
                    card.setType(infoSpans.size() >= 3 ? infoSpans.get(2).text().trim() : "");
                    extractCardDetails(card, cardHtml);
                    cardsAddedInThisSet++;
                }
            }

            final String finalImgSrc = imgSrc;
            boolean variantExists = card.getVariants().stream()
                    .anyMatch(v -> v.getImageUrl() != null && v.getImageUrl().equalsIgnoreCase(finalImgSrc));

            if (!variantExists) {
                CardVariant variant = new CardVariant();
                variant.setCardSet(currentSet);
                variant.setImageUrl(imgSrc);
                variant.setLanguage("EN");

                boolean isParallelUrl = imgSrc.contains("_p") || imgSrc.contains("_sp") || imgSrc.contains("_r");

                if (isParallelUrl) {
                    long parallelCount = card.getVariants().stream()
                            .filter(v -> "Parallel Art".equalsIgnoreCase(v.getArtStyle()) || (v.getArtStyle() != null && v.getArtStyle().startsWith("Parallel Art")))
                            .count();
                    variant.setArtStyle("Parallel Art " + (parallelCount + 1));
                } else {
                    boolean hasNormal = card.getVariants().stream()
                            .anyMatch(v -> "Normal Art".equalsIgnoreCase(v.getArtStyle()));
                    if (!hasNormal) {
                        variant.setArtStyle("Normal Art");
                    } else {
                        variant.setArtStyle("Variant Art " + card.getVariants().size());
                    }
                }

                card.addVariant(variant);
                cardsAddedInThisSet++;
            }

            setCardMap.put(code, card);
        }

        // Salva ou Atualiza todas as cartas do Set sem violar restrições UNIQUE
        cardRepository.saveAll(setCardMap.values());

        currentSet.setTotalCards(cardsInThisSet);
        cardSetRepository.save(currentSet);

        return cardsAddedInThisSet;
    }

    private Map<String, SetInfo> discoverAllSets() {
        Map<String, SetInfo> allUrls = new LinkedHashMap<>();
        try {
            Document doc = Jsoup.connect(BASE_URL).userAgent("Mozilla/5.0").timeout(20000).get();
            Elements options = doc.select("select[name=series] option");

            for (Element option : options) {
                String value = option.attr("value");
                String text = option.text()
                        .replace("<br class=\"spInline\">", " ")
                        .replace("<br>", " ")
                        .replaceAll("\\s+", " ")
                        .trim();

                if (value != null && !value.isEmpty() && !value.equals("0")) {
                    String url = BASE_URL + "?series=" + value;
                    String extractedSetId = null;
                    String extractedSetName = text;

                    // 1. Tentar extrair o ID de entre parênteses rectos: ex "[PRB-01]" ou "[OP-01]"
                    if (text.contains("[") && text.contains("]")) {
                        int start = text.lastIndexOf("[");
                        int end = text.lastIndexOf("]");
                        extractedSetId = text.substring(start + 1, end).trim();
                        extractedSetName = text.substring(0, start).trim();
                    }

                    // 2. Se não encontrou código limpo (ex: Promo/Special), gerar um ID baseado no parâmetro URL (value)
                    if (extractedSetId == null || extractedSetId.isEmpty()) {
                        extractedSetId = "SERIES-" + value;
                    } else {
                        // Garantir maiúsculas para manter consistência (ex: PRB-01, EB-01, OP-01)
                        extractedSetId = extractedSetId.toUpperCase();
                    }

                    // Chave do mapa passa a ser o código exato e único (ex: PRB-01 vs PRB-02)
                    allUrls.put(extractedSetId, new SetInfo(url, extractedSetName));
                }
            }
        } catch (Exception e) {
            System.err.println("🚨 [ERRO] Falha ao mapear lista de Sets: " + e.getMessage());
        }
        return allUrls;
    }

    private void extractCardDetails(Card card, Element cardHtml) {
        Element nameElement = cardHtml.selectFirst(".cardName");
        if (nameElement != null) card.setName(nameElement.text().trim());

        card.setCost(getValueByH3(cardHtml, "Cost"));
        card.setLife(getValueByH3(cardHtml, "Life"));
        card.setPower(getValueByH3(cardHtml, "Power"));
        card.setCounter(getValueByH3(cardHtml, "Counter"));
        card.setColor(getValueByH3(cardHtml, "Color"));
        card.setAttribute(getValueByH3(cardHtml, "Attribute"));
        card.setSubTypes(getValueByH3(cardHtml, "Type"));

        String trigger = getValueByH3(cardHtml, "Trigger");
        card.setTriggerEffect(trigger);

        String effect = getValueByH3(cardHtml, "Effect");
        card.setEffect(effect);

        Element attrImg = cardHtml.selectFirst(".attribute img");
        if (attrImg != null) card.setAttributeIconUrl(attrImg.absUrl("src"));

        Element colorImg = cardHtml.selectFirst(".color img");
        if (colorImg != null) card.setColorIconUrl(colorImg.absUrl("src"));

        Element blockImg = cardHtml.selectFirst(".block img");
        if (blockImg != null) card.setBlockIconUrl(blockImg.absUrl("src"));

        Element blockDiv = cardHtml.selectFirst(".block");
        if (blockDiv != null) {
            Element blockClone = blockDiv.clone();
            blockClone.select("h3").remove();
            String bNum = blockClone.text().trim();
            if (!bNum.isEmpty()) card.setBlockNumber(bNum);
        }

        Set<String> keywordsSet = new LinkedHashSet<>();
        String fullText = (effect != null ? effect : "") + " " + (trigger != null ? trigger : "");

        if (fullText.contains("[")) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\[(.*?)\\]");
            java.util.regex.Matcher m = p.matcher(fullText);
            while (m.find()) {
                String kw = m.group(1).trim();
                if (!kw.isEmpty()) keywordsSet.add(kw);
            }
        }

        if (!keywordsSet.isEmpty()) {
            card.setKeywords(String.join(", ", keywordsSet));
        }
    }

    private String getValueByH3(Element parent, String h3Title) {
        Elements h3s = parent.select("h3");
        for (Element h3 : h3s) {
            if (h3.text().trim().equalsIgnoreCase(h3Title)) {
                Element divParent = h3.parent();
                if (divParent != null) {
                    Element clone = divParent.clone();
                    clone.select("h3").remove();
                    String text = clone.text().trim();
                    return (text.isEmpty() || text.equals("-")) ? null : text;
                }
            }
        }
        return null;
    }
}
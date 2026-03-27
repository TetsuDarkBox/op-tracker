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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // 📝 MÉTODO PRINCIPAL: Extração profunda com Galeria e Keywords
    @Async
    public List<Card> scrapeOfficialSite() {
        System.out.println("🤖 [SISTEMA] A iniciar extração profunda em Modo Galeria...");
        Map<String, Card> cardMap = new HashMap<>();
        Map<String, SetInfo> setsToScrape = discoverNewSets();

        if (setsToScrape.isEmpty()) {
            System.out.println("✅ [INFO] Todos os Sets estão atualizados na Base de Dados.");
            return new ArrayList<>();
        }

        int currentSetIndex = 1;
        for (Map.Entry<String, SetInfo> entry : setsToScrape.entrySet()) {
            String setIdKey = entry.getKey();
            SetInfo info = entry.getValue();

            try {
                System.out.println("🔎 [" + currentSetIndex + "/" + setsToScrape.size() + "] A processar Set: " + info.name);

                // 1. Garantir existência do Set
                CardSet currentSet = cardSetRepository.findBySetId(setIdKey).orElse(new CardSet());
                currentSet.setSetId(setIdKey);
                currentSet.setName(info.name);
                cardSetRepository.save(currentSet);

                Document doc = Jsoup.connect(info.url).userAgent("Mozilla/5.0").timeout(25000).get();
                Elements cardElements = doc.select(".resultCol .modalCol");

                if (cardElements.isEmpty()) {
                    System.out.println("⚠️  [AVISO] Nenhuma carta encontrada no Set: " + info.name);
                    continue;
                }

                int cardsInThisSet = 0;
                for (Element cardHtml : cardElements) {
                    Elements infoSpans = cardHtml.select(".infoCol span");
                    if (infoSpans.isEmpty()) continue;

                    String code = infoSpans.get(0).text().trim();
                    if (code.isEmpty()) continue;

                    // 2. Extração da Imagem
                    Element imgElement = cardHtml.selectFirst(".imgCol img");
                    if (imgElement == null) imgElement = cardHtml.selectFirst(".frontCol img");

                    String imgSrc = "";
                    if (imgElement != null) {
                        imgSrc = imgElement.hasAttr("data-src") ? imgElement.absUrl("data-src") : imgElement.absUrl("src");
                    }

                    // 3. Lógica de Card vs CardVariant (Acumular artes no mesmo Código)
                    Card card = cardMap.get(code);
                    if (card == null) {
                        card = cardRepository.findByCode(code).orElse(new Card());
                        card.setCode(code);
                        card.setRarity(infoSpans.size() >= 2 ? infoSpans.get(1).text().trim() : "");
                        card.setType(infoSpans.size() >= 3 ? infoSpans.get(2).text().trim() : "");

                        extractCardDetails(card, cardHtml);
                        cardsInThisSet++;
                    }

                    // 4. Criar Variante
                    CardVariant variant = new CardVariant();
                    variant.setCardSet(currentSet);
                    variant.setImageUrl(imgSrc);
                    variant.setLanguage("EN");

                    if (imgSrc.contains("_p") || !card.getVariants().isEmpty()) {
                        variant.setArtStyle("Parallel Art " + (card.getVariants().size()));
                    } else {
                        variant.setArtStyle("Normal Art");
                    }

                    card.addVariant(variant);
                    cardMap.put(code, card);
                }

                // Atualizar total e confirmar finalização do Set
                currentSet.setTotalCards(cardsInThisSet);
                cardSetRepository.save(currentSet);
                System.out.println("✅ [" + currentSetIndex++ + "/" + setsToScrape.size() + "] Finalizado: " + info.name + " (" + cardsInThisSet + " cartas únicas)");

                Thread.sleep(2000);

            } catch (Exception e) {
                System.err.println("🚨 [ERRO CRÍTICO] Falha ao extrair Set " + info.name + ": " + e.getMessage());
            }
        }

        printSummary(cardMap.size(), setsToScrape);
        return new ArrayList<>(cardMap.values());
    }

    // 📝 MÉTODO: Descoberta de Sets com limpeza de HTML
    private Map<String, SetInfo> discoverNewSets() {
        Map<String, SetInfo> newUrls = new HashMap<>();
        try {
            Document doc = Jsoup.connect(BASE_URL).userAgent("Mozilla/5.0").get();
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

                    if (text.contains("[") && text.contains("]")) {
                        int start = text.lastIndexOf("[");
                        int end = text.lastIndexOf("]");
                        extractedSetId = text.substring(start + 1, end).trim();
                        extractedSetName = text.substring(0, start).trim();
                    }

                    if (extractedSetId != null) {
                        if (!cardSetRepository.existsBySetId(extractedSetId)) {
                            newUrls.put(extractedSetId, new SetInfo(url, extractedSetName));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("🚨 [ERRO] Falha ao mapear lista de Sets: " + e.getMessage());
        }
        return newUrls;
    }

    private void extractCardDetails(Card card, org.jsoup.nodes.Element cardHtml) {
        // 1. EXTRAÇÃO DO NOME
        org.jsoup.nodes.Element nameElement = cardHtml.selectFirst(".cardName");
        if (nameElement != null) card.setName(nameElement.text().trim());

        // 2. EXTRAÇÃO DE CAMPOS SIMPLES (TEXTO)
        card.setCost(getValueByH3(cardHtml, "Cost"));
        card.setLife(getValueByH3(cardHtml, "Life"));
        card.setPower(getValueByH3(cardHtml, "Power"));
        card.setCounter(getValueByH3(cardHtml, "Counter"));
        card.setColor(getValueByH3(cardHtml, "Color"));
        card.setAttribute(getValueByH3(cardHtml, "Attribute"));
        card.setSubTypes(getValueByH3(cardHtml, "Type"));

        // 3. EXTRAÇÃO DE EFEITOS (TEXTO)
        String trigger = getValueByH3(cardHtml, "Trigger");
        card.setTriggerEffect(trigger);

        String effect = getValueByH3(cardHtml, "Effect");
        card.setEffect(effect);

        // 4. EXTRAÇÃO DE ÍCONES (IMAGENS ESPECÍFICAS)
        org.jsoup.nodes.Element attrImg = cardHtml.selectFirst(".attribute img");
        if (attrImg != null) {
            card.setAttributeIconUrl(attrImg.absUrl("src"));
        }

        org.jsoup.nodes.Element blockImg = cardHtml.selectFirst(".block img");
        if (blockImg != null) {
            card.setBlockIconUrl(blockImg.absUrl("src"));
        }

        // Número do Bloco em texto
        org.jsoup.nodes.Element blockDiv = cardHtml.selectFirst(".block");
        if (blockDiv != null) {
            org.jsoup.nodes.Element blockClone = blockDiv.clone();
            blockClone.select("h3").remove();
            String bNum = blockClone.text().trim();
            if (!bNum.isEmpty()) card.setBlockNumber(bNum); // Agora já existe no Card.java!
        }

        // 5. SCANNER DE KEYWORDS
        java.util.Set<String> keywordsSet = new java.util.LinkedHashSet<>();
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

    private void printSummary(int totalCards, Map<String, SetInfo> processedSets) {
        System.out.println("\n=================================================");
        System.out.println("🏴‍☠️ [RELATÓRIO FINAL DE EXTRAÇÃO] 🏴‍☠️");
        System.out.println("=================================================");
        System.out.println("📦 Cartas Únicas: " + totalCards);
        System.out.println("🗺️  Sets Adicionados: " + processedSets.size());
        System.out.println("📸 Galeria: Todas as variantes de imagem guardadas.");
        System.out.println("=================================================\n");
    }
}
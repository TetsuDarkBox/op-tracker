# OP Tracker - Configuration Example
spring.application.name=op-tracker-api

# Base de Dados (Substituir pelos teus dados do TiDB)
spring.datasource.url=jdbc:mysql://HOST_AQUI:4000/NOMEDABD?sslMode=VERIFY_IDENTITY
spring.datasource.username=TEU_USER
spring.datasource.password=TUA_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
# Demo - 書籍・著者管理API

Spring Boot + jOOQ + PostgreSQL を使った書籍・著者管理REST APIのデモアプリケーションです。

## 前提条件

- Java 21以上
- Docker / Docker Compose

## 起動方法

1. `docker compose up -d`
2. `./gradlew jooqCodegen`
3. `./gradlew bootRun`

## API仕様

`src/main/resources/openapi.yaml` を参照してください。

## データベースのリセット

```bash
docker compose down -v
```

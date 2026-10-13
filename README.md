# inventory-app

Sample **Inventory Management** web application for the *EAD – DevOps Fundamentals with GitHub Actions and Docker* lab.

Built with Java 17, Spring Boot 3.5, Spring MVC + Thymeleaf, Spring Data JPA, an in-memory H2 database and JUnit 5 / Mockito.
No external database is required, so the app starts the same way on a laptop, in GitHub Actions and in Docker.

## Build, test, run

```bash
mvn clean install        # compile + run all tests + build target/inventory-app-1.0.0.jar  -> BUILD SUCCESS
mvn test                 # run unit tests only (Tests run: 26, Failures: 0)
mvn spring-boot:run      # run the app           (or: java -jar target/inventory-app-1.0.0.jar)
```

Then open:

| URL | What |
| --- | --- |
| http://localhost:8080/ | Inventory web page (list, add, adjust stock, delete) |
| http://localhost:8080/api/products | REST API (JSON) |
| http://localhost:8080/actuator/health | Health check (`{"status":"UP"}`) |

Six sample products are loaded at startup. Data lives in memory and resets on every restart.

## REST API

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/products` | List all products |
| GET | `/api/products/{id}` | Get one product (404 if missing) |
| GET | `/api/products/low-stock` | Products at or below their reorder level |
| GET | `/api/products/summary` | Totals: products, units, stock value, low-stock count |
| POST | `/api/products` | Create a product (400 on validation error, 409 on duplicate SKU) |
| PUT | `/api/products/{id}` | Replace a product |
| PATCH | `/api/products/{id}/stock?delta=-5` | Add / remove stock (409 if it would go negative) |
| DELETE | `/api/products/{id}` | Delete a product |

Example:

```bash
curl -X POST localhost:8080/api/products -H "Content-Type: application/json" \
  -d '{"name":"Webcam","sku":"ELEC-010","category":"Electronics","quantity":15,"price":9500.00,"reorderLevel":5}'
```

## Project layout

```
src/main/java/com/abcretail/inventory
├── InventoryApplication.java        entry point
├── model/Product.java               JPA entity + validation rules
├── repository/ProductRepository     Spring Data JPA
├── service/InventoryService         business logic (stock rules, totals)
├── util/Calculator                  tiny helper used in the lab's "break the test" exercise
├── controller/api/                  REST controller
├── controller/web/                  Thymeleaf page controller
├── exception/                       custom exceptions + JSON error handler
└── config/DataLoader                sample data
src/test/java/...                    JUnit 5 tests (Calculator, Service, REST controller, Repository, app smoke test)
```

## Lab notes

* The starter intentionally has **no** `Dockerfile` and **no** `.github/workflows` folder – students create these in Tasks 4 and 6.
* The Dockerfile from the lab sheet uses `COPY target/*.jar app.jar`, so run `mvn clean install` **before** `docker build`.
* Run the container with a published port to open it in a browser: `docker run -p 8080:8080 inventory-app`.

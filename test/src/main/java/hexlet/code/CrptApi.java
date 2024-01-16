package hexlet.code;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.*;

public class CrptApi {
    private final Semaphore semaphore;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);

        // Запускаем отдельный поток для обновления разрешений в соответствии с интервалом
        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(timeUnit.toSeconds(1));
                    semaphore.release(); // Обновляем разрешение каждую секунду
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public void createDocument(Document document, String signature) {
        try {
            semaphore.acquire();
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonDocument = objectMapper.writeValueAsString(document);

            // Вывод в консоль
            System.out.println("Симуляция API для создания документа");
            System.out.println("Документ: " + jsonDocument);
            System.out.println("Подписб: " + signature);
            System.out.println("Документ успешно создан.");

        } catch (InterruptedException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Getter
    @Setter
    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Product> products;
        private String reg_date;
        private String reg_number;
        private String signature;
    }

    @Getter
    @Setter
    public static class Description {
        @JsonProperty("participantInn")
        private String participantInn;
    }

    @Getter
    @Setter
    public static class Product {
        @JsonProperty("certificate_document")
        private String certificateDocument;
        @JsonProperty("certificate_document_date")
        private String certificateDocumentDate;
        @JsonProperty("certificate_document_number")
        private String certificateDocumentNumber;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private String productionDate;
        @JsonProperty("tnved_code")
        private String tnvedCode;
        @JsonProperty("uit_code")
        private String uitCode;
        @JsonProperty("uitu_code")
        private String uituCode;
    }

    public static void main(String[] args) {
        // Создаем экземпляр CrptApi
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5); // Пример: 5 запросов в секунду

        // Создаем тестовые данные для документа
        CrptApi.Document document = new CrptApi.Document();
        document.setDoc_id("123456");
        document.setDoc_status("Draft");
        document.setDoc_type("LP_INTRODUCE_GOODS");
        document.setImportRequest(true);
        document.setOwner_inn("1234567890");
        document.setParticipant_inn("0987654321");
        document.setProducer_inn("1112223334");
        document.setProduction_date("2020-01-23");
        document.setProduction_type("SomeType");

        CrptApi.Product product = new CrptApi.Product();
        product.setCertificateDocument("Cert123");
        product.setCertificateDocumentDate("2020-01-23");
        product.setCertificateDocumentNumber("CertNumber123");
        product.setOwnerInn("1234567890");
        product.setProducerInn("1112223334");
        product.setProductionDate("2020-01-23");
        product.setTnvedCode("SomeCode");
        product.setUitCode("UitCode123");
        product.setUituCode("UituCode456");


        document.setProducts(List.of(product));

        document.setReg_date("2020-01-23");
        document.setReg_number("RegNumber123");
        document.setSignature("testSignature");

        // Вызываем метод createDocument
        crptApi.createDocument(document, document.getSignature());
    }
}

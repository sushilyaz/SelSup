package hexlet.code;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApiSec {
    private final Semaphore semaphore;

    public CrptApiSec(TimeUnit timeUnit, int requestLimit) {
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

            String apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";
            String requestBody = createRequestBody(document, signature);

            HttpResponse response = sendHttpPostRequest(apiUrl, requestBody);

            handleApiResponse(response);

            System.out.println("Документ успешно создан.");

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private String createRequestBody(Document document, String signature) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(document);
    }

    private HttpResponse sendHttpPostRequest(String apiUrl, String requestBody) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiUrl);

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(requestBody));

        return httpClient.execute(httpPost);
    }

    private void handleApiResponse(HttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("Status Code: " + statusCode);

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                String line;
                StringBuilder responseContent = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }

                // Обработка тела ответа
                String responseBody = responseContent.toString();
                System.out.println("Response Body: " + responseBody);
            } catch (IOException e) {
                e.printStackTrace(); // Обработка ошибок по вашему усмотрению
            }
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
        CrptApiSec crptApiSec = new CrptApiSec(TimeUnit.SECONDS, 5); // Пример: 5 запросов в секунду

        // Создаем тестовые данные для документа
        CrptApiSec.Document document = new CrptApiSec.Document();
        document.setDoc_id("123456");
        document.setDoc_status("Draft");
        document.setDoc_type("LP_INTRODUCE_GOODS");
        document.setImportRequest(true);
        document.setOwner_inn("1234567890");
        document.setParticipant_inn("0987654321");
        document.setProducer_inn("1112223334");
        document.setProduction_date("2020-01-23");
        document.setProduction_type("SomeType");

        CrptApiSec.Product product = new CrptApiSec.Product();
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
        crptApiSec.createDocument(document, document.getSignature());
    }
}


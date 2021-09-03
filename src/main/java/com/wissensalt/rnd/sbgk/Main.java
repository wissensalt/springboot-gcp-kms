package com.wissensalt.rnd.sbgk;

import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.DecryptResponse;
import com.google.cloud.kms.v1.EncryptResponse;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SpringBootApplication
public class Main {

    private final EmployeeRepository employeeRepository;

    @Value("${gcp.kms.projectId}")
    private String projectId;

    @Value("${gcp.kms.locationId}")
    private String locationId;

    @Value("${gcp.kms.keyRingId}")
    private String keyRingId;

    @Value("${gcp.kms.keyId}")
    private String keyId;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @PostMapping("/save")
    private Employee save(@RequestBody Employee employee) throws IOException {
        employee.setEmployeeId(convertToString(encrypt(employee.getEmployeeId())));

        return employeeRepository.save(employee);
    }

    @GetMapping
    private List<Employee> getEmployees() {
        final List<Employee> employees = employeeRepository.findAll();

        return employees
                .stream()
                .map(employee -> {
                    try {
                        employee.setEmployeeId(decrypt(convertToByteString(employee.getEmployeeId())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return employee;
                }).collect(Collectors.toList());
    }

    private static ByteString convertToByteString(String decrypted) {
        return ByteString.copyFrom(Base64.getDecoder().decode(decrypted));
    }

    private static String convertToString(ByteString encrypted) {
        return Base64.getEncoder().encodeToString(encrypted.toByteArray());
    }
    private  ByteString encrypt(String plaintext) throws IOException {
        EncryptResponse response;

        try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
            CryptoKeyName keyVersionName = CryptoKeyName.of(projectId, locationId, keyRingId, keyId);
            response = client.encrypt(keyVersionName, ByteString.copyFromUtf8(plaintext));

            return response.getCiphertext();
        }
    }

    private String decrypt(ByteString chipperText) throws IOException {
        try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
            CryptoKeyName keyName = CryptoKeyName.of(projectId, locationId, keyRingId, keyId);
            DecryptResponse response = client.decrypt(keyName, chipperText);

            return response.getPlaintext().toStringUtf8();
        }
    }
}

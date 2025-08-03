// @CrossOrigin(origins = "http://localhost:5173") // Update if frontend port changes
// @CrossOrigin(origins = "https://keen-alfajores-37227d.netlify.app")

// src/main/java/com/nishith/dashboardbackend/DashboardController.java

// package com.nishith.dashboardbackend;
// import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.springframework.core.io.ClassPathResource;
// import org.springframework.core.io.Resource;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.IOException;
// import java.io.InputStream;
// // import java.nio.file.*;
// import java.util.List;
// import java.util.Objects;
// import java.util.stream.Collectors;

// @RestController
// @RequestMapping("/api/json")
// // @CrossOrigin(origins = "http://localhost:5173") // Update if frontend port changes
// @CrossOrigin(origins = "https://keen-alfajores-37227d.netlify.app")
// public class DashboardController {

//     private final ObjectMapper objectMapper = new ObjectMapper();

//     // ✅ Load a specific JSON file
//     @GetMapping("/{filename}")
//     public ResponseEntity<?> getJsonData(@PathVariable String filename) {
//         try {
//             if (!filename.endsWith(".json")) {
//                 filename += ".json";
//             }

//             Resource resource = new ClassPathResource("data/" + filename);
//             if (!resource.exists()) {
//                 return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                         .body("Error: File '" + filename + "' not found.");
//             }

//             try (InputStream inputStream = resource.getInputStream()) {
//                 Object jsonData = objectMapper.readValue(inputStream, Object.class);
//                 return ResponseEntity.ok(jsonData);
//             }

//         } catch (IOException e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body("Error reading file: " + e.getMessage());
//         }
//     }

//     // 🆕 BONUS: List available JSON files from /resources/data
//     @GetMapping("/files")
//     public ResponseEntity<?> listAvailableFiles() {
//         try {
//             PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//             Resource[] resources = resolver.getResources("classpath:data/*.json");

//             List<String> filenames = 
//                 List.of(resources).stream()
//                     .map(res -> {
//                         try {
//                             return res.getFilename();
//                         } catch (Exception e) {
//                             return null;
//                         }
//                     })
//                     .filter(Objects::nonNull)
//                     .map(name -> name.replace(".json", ""))
//                     .collect(Collectors.toList());

//             return ResponseEntity.ok(filenames);

//         } catch (IOException e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body("Error listing files: " + e.getMessage());
//         }
//     }
// }


// package com.nishith.dashboardbackend;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.springframework.http.*;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.client.RestTemplate;

// // import java.util.*;

// @RestController
// @RequestMapping("/api/json")
// @CrossOrigin(origins = "https://keen-alfajores-37227d.netlify.app") // Your frontend URL
// public class DashboardController {

//     private final ObjectMapper objectMapper = new ObjectMapper();

//     // ✅ S3 base URL (all files are public)
//     private static final String S3_BASE_URL = "https://dashboard-jsons-file.s3.eu-north-1.amazonaws.com/";

//     private final RestTemplate restTemplate = new RestTemplate();

//     // ✅ Fetch JSON from S3
//     @GetMapping("/{filename}")
//     public ResponseEntity<?> getJsonData(@PathVariable String filename) {
//         try {
//             if (!filename.endsWith(".json")) {
//                 filename += ".json";
//             }

//             String url = S3_BASE_URL + filename;

//             ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

//             if (response.getStatusCode().is2xxSuccessful()) {
//                 // Optional: parse to Object to ensure valid JSON
//                 Object parsedJson = objectMapper.readValue(response.getBody(), Object.class);
//                 return ResponseEntity.ok(parsedJson);
//             } else {
//                 return ResponseEntity.status(response.getStatusCode())
//                         .body("Error fetching file from S3.");
//             }

//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body("Error fetching or parsing JSON: " + e.getMessage());
//         }
//     }

//     // ❌ This old local file listing method is no longer useful with S3
//     // Feel free to delete it if not needed
// }

package com.nishith.dashboardbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/json")
@CrossOrigin(origins = {"http://localhost:5173", "https://keen-alfajores-37227d.netlify.app"}) // Your frontend URL
public class DashboardController {

    private static final String S3_BASE_URL = "https://dashboard-jsons-file.s3.eu-north-1.amazonaws.com/";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ Fetch any JSON file from S3
    @GetMapping("/{filename}")
    public ResponseEntity<?> getJsonFromS3(@PathVariable String filename) {
        try {
            if (!filename.endsWith(".json")) {
                filename += ".json";
            }

            String fileUrl = S3_BASE_URL + filename;
            ResponseEntity<String> response = restTemplate.getForEntity(fileUrl, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Object parsedJson = objectMapper.readValue(response.getBody(), Object.class);
                return ResponseEntity.ok(parsedJson);
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Failed to fetch file from S3: " + fileUrl);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching or parsing file: " + e.getMessage());
        }
    }

    // ✅ Fetch the list of all dashboard filenames (from files.json in S3)
    @GetMapping("/files")
    public ResponseEntity<?> getFileList() {
        try {
            String url = S3_BASE_URL + "files.json";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Object filenames = objectMapper.readValue(response.getBody(), Object.class);
                return ResponseEntity.ok(filenames);
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Failed to fetch files.json");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching file list: " + e.getMessage());
        }
    }
}












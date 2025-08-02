// package com.nishith.dashboardbackend;

// import org.springframework.core.io.ClassPathResource;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.charset.StandardCharsets;

// @RestController
// @RequestMapping("/api/dashboard")
// @CrossOrigin(origins = "http://localhost:5174")
// public class DashboardController {

//     @GetMapping("/{filename}")
//     public ResponseEntity<?> getJsonData(@PathVariable String filename) {
//         try {
//             // Read the file from resources/data folder
//             ClassPathResource resource = new ClassPathResource("data/" + filename + ".json");
//             InputStream inputStream = resource.getInputStream();
//             String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

//             // Return raw JSON as response
//             return ResponseEntity.ok().body(json);
//         } catch (IOException e) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
//         }
//     }
// }


// package com.nishith.dashboardbackend;

// import org.springframework.core.io.ClassPathResource;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.charset.StandardCharsets;

// @RestController
// @RequestMapping("/api/json")
// // @CrossOrigin(origins = "http://localhost:5173") // Allow frontend to call backend

// // @CrossOrigin(origins = "http://localhost:*")
// @CrossOrigin(origins = "http://localhost:5176")
// public class DashboardController {

//     @GetMapping("/{filename:.+\\.json}")
//     public ResponseEntity<?> getJsonData(@PathVariable("filename") String filename) {
//         try {
//             // Read the file from src/main/resources/data folder
//             ClassPathResource resource = new ClassPathResource("data/" + filename);
//             InputStream inputStream = resource.getInputStream();
//             String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

//             return ResponseEntity.ok().body(json);
//         } catch (IOException e) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                                  .body("Error: File '" + filename + "' not found.");
//         }
//     }
// }




// package com.nishith.dashboardbackend;

// import org.springframework.core.io.ClassPathResource;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.charset.StandardCharsets;

// @RestController
// @RequestMapping("/api/json")
// @CrossOrigin(origins = "http://localhost:5173") // Adjust if needed
// public class DashboardController {

//     @GetMapping("/{filename:.+\\.json}")
//     public ResponseEntity<?> getJsonData(@PathVariable("filename") String filename) {
//         try {
//             // Load JSON file from src/main/resources/data/
//             ClassPathResource resource = new ClassPathResource("data/" + filename);
//             InputStream inputStream = resource.getInputStream();

//             // Convert to String
//             String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
//             return ResponseEntity.ok().body(json);

//         } catch (IOException e) {
//             // Return 404 if file not found
//             return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                                  .body("Error: File '" + filename + "' not found.");
//         }
//     }
// }



// src/main/java/com/nishith/dashboardbackend/DashboardController.java

package com.nishith.dashboardbackend;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
// import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/json")
@CrossOrigin(origins = "http://localhost:5173") // Update if frontend port changes
public class DashboardController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ Load a specific JSON file
    @GetMapping("/{filename}")
    public ResponseEntity<?> getJsonData(@PathVariable String filename) {
        try {
            if (!filename.endsWith(".json")) {
                filename += ".json";
            }

            Resource resource = new ClassPathResource("data/" + filename);
            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Error: File '" + filename + "' not found.");
            }

            try (InputStream inputStream = resource.getInputStream()) {
                Object jsonData = objectMapper.readValue(inputStream, Object.class);
                return ResponseEntity.ok(jsonData);
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading file: " + e.getMessage());
        }
    }

    // 🆕 BONUS: List available JSON files from /resources/data
    @GetMapping("/files")
    public ResponseEntity<?> listAvailableFiles() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:data/*.json");

            List<String> filenames = 
                List.of(resources).stream()
                    .map(res -> {
                        try {
                            return res.getFilename();
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .map(name -> name.replace(".json", ""))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(filenames);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error listing files: " + e.getMessage());
        }
    }
}

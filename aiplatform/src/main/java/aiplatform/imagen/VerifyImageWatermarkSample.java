/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aiplatform.imagen;

// [START generativeaionvertexai_imagen_verify_image_watermark]

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VerifyImageWatermarkSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String inputPath = "/path/to/my-input.png";

    verifyImageWatermark(projectId, location, inputPath);
  }

  // Verify if an image contains a digital watermark. By default, a non-visible, digital watermark
  // (called a SynthID) is added to images generated by a model version that supports
  //  watermark generation.
  public static PredictResponse verifyImageWatermark(
      String projectId, String location, String inputPath) throws ApiException, IOException {
    final String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    PredictionServiceSettings predictionServiceSettings =
        PredictionServiceSettings.newBuilder().setEndpoint(endpoint).build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (PredictionServiceClient predictionServiceClient =
        PredictionServiceClient.create(predictionServiceSettings)) {

      final EndpointName endpointName =
          EndpointName.ofProjectLocationPublisherModelName(
              projectId, location, "google", "imageverification@001");

      // Encode image to Base64
      String imageBase64 =
          Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(inputPath)));

      // Create the image map
      Map<String, String> imageMap = new HashMap<>();
      imageMap.put("bytesBase64Encoded", imageBase64);

      Map<String, Object> instancesMap = new HashMap<>();
      instancesMap.put("image", imageMap);
      Value instances = mapToValue(instancesMap);

      // Optional parameters
      Map<String, Object> paramsMap = new HashMap<>();
      Value parameters = mapToValue(paramsMap);

      PredictResponse predictResponse =
          predictionServiceClient.predict(
              endpointName, Collections.singletonList(instances), parameters);

      for (Value prediction : predictResponse.getPredictionsList()) {
        Map<String, Value> fieldsMap = prediction.getStructValue().getFieldsMap();
        if (fieldsMap.containsKey("decision")) {
          // "ACCEPT" if the image contains a digital watermark
          // "REJECT" if the image does not contain a digital watermark
          System.out.format(
              "Watermark verification result: %s", fieldsMap.get("decision").getStringValue());
        }
      }
      return predictResponse;
    }
  }

  private static Value mapToValue(Map<String, Object> map) throws InvalidProtocolBufferException {
    Gson gson = new Gson();
    String json = gson.toJson(map);
    Value.Builder builder = Value.newBuilder();
    JsonFormat.parser().merge(json, builder);
    return builder.build();
  }
}

// [END generativeaionvertexai_imagen_verify_image_watermark]

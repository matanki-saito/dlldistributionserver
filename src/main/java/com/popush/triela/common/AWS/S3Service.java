package com.popush.triela.common.AWS;

import com.popush.triela.common.Exception.AWSException;
import com.popush.triela.common.Exception.MachineException;
import com.popush.triela.common.Exception.OtherSystemException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3;

    public void upload(Path file, String bucketName, String key) throws OtherSystemException {
        try {
            s3.putObject(
                    PutObjectRequest.builder().bucket(bucketName).key(key).build(),
                    RequestBody.fromFile(file)
            );
        } catch (AwsServiceException e) {
            throw new AWSException("aws service exception", e);
        } catch (SdkClientException e) {
            throw new MachineException("aws inner exception", e);
        }
    }
}

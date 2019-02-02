package com.popush.triela.common.github;

import com.popush.triela.Manager.distribution.DistFileFormatV1;
import com.popush.triela.Manager.distribution.DistributionService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import retrofit2.Retrofit;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class GitHubApiServiceTest {

    @InjectMocks
    DistributionService distributionService;

    @InjectMocks
    GitHubApiService gitHubApiService;

    @Mock
    GitHubApiMapper gitHubApiMapper;

    @Test
    public void salvageDistFileFromAssetFile() throws Exception {
        Optional<DistFileFormatV1> result = distributionService.salvageDistFileV1FromAsset(
                Paths.get(new ClassPathResource("test.zip").getURI())
        );

        assertThat(result).isEqualTo(
                Optional.of(
                        DistFileFormatV1
                                .builder()
                                .filter(Arrays.asList("\\.png$"))
                                .isArchive(Boolean.TRUE)
                                .build()
                )
        );
    }

    @Test
    public void salvageFilesFromAssetFile() throws Exception {

        Map<Path, Path> result = distributionService.salvageFilesFromAssetFile(
                Paths.get(new ClassPathResource("test.zip").getURI()),
                DistFileFormatV1.builder().filter(Arrays.asList("\\.png$", "\\.jpg$", "reimu.dat$")).isArchive(true).build()
        );

        assertThat(result).containsKeys(
                Path.of("tiruno.png"),
                Path.of("homugeso/misuzu.jpg"),
                Path.of("homugeso/reimu.dat")
        );
    }

    @Test
    public void testConcreteZip() throws Exception {

        Path f1 = Files.createTempFile("triela_test", ".tmp");
        Path f2 = Files.createTempFile("triela_test", ".tmp");
        Path f3 = Files.createTempFile("triela_test", ".tmp");

        final Path result = distributionService.concreteZip(Map.of(
                Path.of("test/tiruno.png"), f1,
                Path.of("test/homugeso/misuzu.jpg"), f2,
                Path.of("test/homugeso/reimu.dat"), f3
        ));

        assertThat(result).isNotNull();
    }

    @Test
    public void getAsset() throws Exception {

        final MockWebServer mockWebServer = new MockWebServer();
        final MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(200).setBody("123");
        mockWebServer.enqueue(mockResponse);
        mockWebServer.start();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        GitHubApiMapper baseGitHubApiMapper = retrofit.create(GitHubApiMapper.class);

        Mockito.when(gitHubApiMapper.downloadFileWithDynamicUrlSync(Mockito.any())).thenReturn(
                baseGitHubApiMapper.downloadFileWithDynamicUrlSync(URI.create("dummy"))
        );

        gitHubApiService.getAssetFile(URI.create("https://github.com/testuser1200/test/releases/download/v4/homugeso.zip"));
    }
}
package com.popush.triela.common.github;

import com.popush.triela.Manager.distribution.DistFileFormatV1;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class GitHubApiServiceTest {

    @InjectMocks
    GitHubApiService gitHubApiService;

    @Test
    public void testSalvageDistFileFromZipInputStream() throws Exception {

        try (InputStream inputStream = new ClassPathResource("test.zip").getInputStream()) {

            Optional<DistFileFormatV1> result = gitHubApiService.salvageDistFileV1FromInputStream(inputStream);

            assertThat(result).isEqualTo(
                    Optional.of(
                            DistFileFormatV1
                                    .builder()
                                    .filter(Arrays.asList("*.png"))
                                    .isArchive(Boolean.TRUE)
                                    .build()
                    )
            );
        }
    }

    @Test
    public void testSalvageFilesFromInputStream() throws Exception {

        try (InputStream inputStream = new ClassPathResource("test.zip").getInputStream()) {
            Map<Path, byte[]> result = gitHubApiService.salvageFilesFromInputStream(
                    inputStream,
                    DistFileFormatV1.builder().filter(Arrays.asList("\\.png$", "\\.jpg$", "reimu.dat$")).isArchive(true).build()
            );

            assertThat(result).containsEntry(Path.of("test/tiruno.png"), new byte[]{1, 2, 3, 4, 5});
            assertThat(result).containsEntry(Path.of("test/homugeso/misuzu.jpg"), new byte[]{1, 2, 3, 4, 5});
            assertThat(result).containsEntry(Path.of("test/homugeso/reimu.dat"), new byte[]{1, 2, 3, 4, 5});
        }
    }

    @Test
    public void testConcreteZip() throws Exception {
        byte[] result = gitHubApiService.concreteZip(Map.of(
                Path.of("test/tiruno.png"), new byte[]{1, 2, 3, 4, 5},
                Path.of("test/homugeso/misuzu.jpg"), new byte[]{1, 2, 3, 4, 5},
                Path.of("test/homugeso/reimu.dat"), new byte[]{1, 2, 3, 4, 5}
        ));

        try (FileOutputStream fio = new FileOutputStream(new File("result.zip"))) {
            fio.write(result);
        }

        assertThat(result).isNotEmpty();
    }
}
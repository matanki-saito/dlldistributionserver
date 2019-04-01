package com.popush.triela.test;

import com.popush.triela.common.db.ExeSelectCondition;
import com.popush.triela.db.ExeDao;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SampleTest {

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    /* こうしないとIntellijでアラートが出る */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ExeDao dao;

    @Test
    public void simpleTest() {
//        dao.upsert(ExeDto.builder()
//                .id(1)
//                .description("hoge")
//                .distributionAssetId(1)
//                .gitHubRepoId(1)
//                .md5("123456")
//                .phase("dev")
//                .version("1.0.0.0")
//                .build()
//        );
        var list = dao.list(ExeSelectCondition.builder().build());

        softly.assertThat(list.size()).isEqualTo(1);

    }
}

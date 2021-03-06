package com.popush.triela.test;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.junit4.SpringRunner;

import com.popush.triela.common.db.ExeSelectCondition;
import com.popush.triela.db.ExeMapper;

@RunWith(SpringRunner.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SampleTest {

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    /* こうしないとIntellijでアラートが出る */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ExeMapper exeMapper;

    @Test
    public void simpleTest() {
        var list = exeMapper.selectByCondition(ExeSelectCondition.builder().id(1).build(), 0L, 10000);

        softly.assertThat(list.size()).isEqualTo(1);
    }
}

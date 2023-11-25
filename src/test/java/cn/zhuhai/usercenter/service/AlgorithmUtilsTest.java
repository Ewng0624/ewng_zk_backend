package cn.zhuhai.usercenter.service;

import cn.zhuhai.usercenter.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @Author Ewng
 * @Date 2023/11/25 11:53
 */
@SpringBootTest
public class AlgorithmUtilsTest {
    @Test
    void test01() {
        String str1 = "你吃饭了没";
        String str2 = "你吃饭了";
        String str3 = "你有没有吃饭";
        int tmp1 = AlgorithmUtils.minDistance(str1, str2);
        int tmp2 = AlgorithmUtils.minDistance(str2, str3);
        int tmp3 = AlgorithmUtils.minDistance(str1, str3);
        System.out.println(tmp1);
        System.out.println(tmp2);
        System.out.println(tmp3);
        String str4 = "horse";
        String str5 = "more";
        int score = AlgorithmUtils.minDistance(str4, str5);
        System.out.println(score);
    }

    @Test
    void testCompareTags() {
        List<String> tagList1 = Arrays.asList("Java", "大一", "男");
        List<String> tagList2 = Arrays.asList("Java", "大一", "女");
        List<String> tagList3 = Arrays.asList("Python", "大二", "女");
        // 1
        int score1 = AlgorithmUtils.minDistance(tagList1, tagList2);
        // 3
        int score2 = AlgorithmUtils.minDistance(tagList1, tagList3);
        System.out.println(score1);
        System.out.println(score2);
    }
}

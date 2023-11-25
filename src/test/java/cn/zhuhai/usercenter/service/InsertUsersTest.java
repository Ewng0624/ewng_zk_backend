//package cn.zhuhai.usercenter.service;
//
//import cn.zhuhai.usercenter.mapper.UserMapper;
//import cn.zhuhai.usercenter.model.domain.User;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.*;
//
//
///**
// * @Author Ewng
// * @description 一次性操作  打包时记得注释掉
// * @Date 2023/11/20 15:39
// */
//@SpringBootTest
//public class InsertUsersTest {
//
//    @Resource
//    private UserMapper userMapper;
//
//
//    @Resource
//    private UserService userService;
//
//    // cpu密集型: 分配的核心线程数 = CPU - 1
//    // io密集型: 分配的核心线程数可以大于CPU核数
//    private ExecutorService executorService = new ThreadPoolExecutor(60,
//            1000,
//            10000,
//            TimeUnit.MINUTES,
//            new ArrayBlockingQueue<>(10000));
//    /**
//     * 批量插入数据(for 线性插入)
//     */
//    @Test
//    void doInsertUsersTest01() {
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 1000; i++) {
//            User user = new User();
//            user.setUsername("假用户"+i);
//            user.setUserAccount("fakeEwng");
//            user.setHeadUrl("");
//            user.setGender(0);
//            user.setUserPassword("12345678");
//            user.setPhone("123");
//            user.setEmail("456");
//            user.setUserStatus(0);
//            user.setTags("[]");
//            userMapper.insert(user);
//        }
//        long end = System.currentTimeMillis();
//        System.out.println(end-start);//4323ms
//    }
//
//    /**
//     * 批量插入数据
//     */
//    @Test
//    void doInsertUsersTest02() {
//        long start = System.currentTimeMillis();
//        List<User> userList = new ArrayList<>();
//        final int INSERT_NUM = 100000;
//        for (int i = 0; i < INSERT_NUM; i++) {
//            User user = new User();
//            user.setUsername("假用户"+i);
//            user.setUserAccount("fakeEwng");
//            user.setHeadUrl("");
//            user.setGender(0);
//            user.setUserPassword("12345678");
//            user.setPhone("123");
//            user.setEmail("456");
//            user.setUserStatus(0);
//            user.setTags("[]");
//            userList.add(user);
//        }
//        userService.saveBatch(userList, 50000);
//        long end = System.currentTimeMillis();
//        // 1000 -> 1044ms
//        // 10w 1000batchSize-> 17667ms
//        // 10w 1000batchSize-> 17048ms
//        // 10w 50000batchSize -> 17315ms
//        System.out.println(end-start);//1044ms
//    }
//
//    /**
//     * 并发插入数据
//     */
//    @Test
//    void doInsertUsersTest03() {
//        long start = System.currentTimeMillis();
//        // 分十组
//        int j = 0;
//        List<CompletableFuture<Void>> futureList = new ArrayList<>();
//        int batchSize = 5000;
//        for (int i = 0; i < 20; i++) {
//            List<User> userList = new ArrayList<>();
//            while (true) {
//                j++;
//                User user = new User();
//                user.setUsername("假用户"+i);
//                user.setUserAccount("fakeEwng");
//                user.setHeadUrl("");
//                user.setGender(0);
//                user.setUserPassword("12345678");
//                user.setPhone("123");
//                user.setEmail("456");
//                user.setUserStatus(0);
//                user.setTags("[]");
//                userList.add(user);
//                if (j % batchSize == 0) {
//                    break;
//                }
//            }
//            // 异步执行
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
//                System.out.println("ThreadName: "+Thread.currentThread().getName());
//                userService.saveBatch(userList, batchSize);
//            }, executorService);
//            futureList.add(future);
//        }
//        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
//        long end = System.currentTimeMillis();
//        // 10w 10000batchSize-> 11743ms
//        // 10w 10000batchSize-> 7081ms  关闭输出日志
//        // 10w 5000batchSize-> 6667ms  关闭输出日志
//        // 10w 4000batchSize-> 6489ms  关闭输出日志
//        // 10w 2500batchSize-> 8181ms  关闭输出日志
//        // 10w 2500batchSize-> 6812ms  关闭输出日志 自定义线程池
//        // 10w 5000batchSize-> 8972ms  关闭输出日志 自定义线程池
//
//        System.out.println(end-start);
//    }
//}
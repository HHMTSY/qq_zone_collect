package com.h.qq.services;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.h.qq.mapper.ResultMapper;
import com.h.qq.mapper.SsXcRzMapper;
import com.h.qq.mapper.ZoneExceptionMapper;
import com.h.qq.pojo.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hhm
 * @date 2024/8/28
 * @description TODO
 */
@Service
public class MainService {

    @Autowired
    private ResultMapper resultMapper;

    @Autowired
    private ZoneExceptionMapper zoneExceptionMapper;

    @Autowired
    private SsXcRzMapper ssXcRzMapper;

//    本人 qq号
    private static final String qqNum = "";
//    https://user.qzone.qq.com/proxy/domain/ic2.qzone.qq.com/cgi-bin/feeds/feeds3_html_more 接口携带
    private static final String g_tk = "";
    private static final String cookie = "";
    public  List<MyFriend> getFriends() {
        String url = "https://user.qzone.qq.com/proxy/domain/r.qzone.qq.com/cgi-bin/tfriend/friend_show_qqfriends.cgi" +
                "?uin=" + qqNum +
                "&follow_flag=0" +
                "&groupface_flag=0" +
                "&fupdate=1" +
                "&g_tk=" + g_tk +
                "&g_tk=" + g_tk;
        try {
            System.out.println("好友列表请求发起...");
            // 使用jsoup连接网站并获取页面内容
            Document doc = Jsoup.connect(url).cookie("cookie", cookie).get();

            String html = doc.body().html();
            String substring = html.substring(10);
            String substring1 = substring.substring(0, substring.length() - 2);

            //substring1转json
            String jsonStr = JSONUtil.toJsonStr(substring1);
            Response bean = JSONUtil.toBean(jsonStr, Response.class);

            //好友列表
            List<MyFriend> myFriends = bean.getData().getItems();
            System.out.println("好友列表请求结束,好友数量"+myFriends.size());
            System.out.println(myFriends);

            return myFriends;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //说说页面,html
    public void visitZone(){


        String url = "https://user.qzone.qq.com/proxy/domain/ic2.qzone.qq.com/cgi-bin/feeds/feeds_html_module" +
                "?g_iframeUser=1&i_uin={0}" + //访问目标
                "&i_login_uin=" +qqNum+ //登录用户
                "&mode=4&previewV8=1" +
                "&style=35&version=8" +
                "&needDelOpr=true&transparence=true" +
                "&hideExtend=false&showcount=5" +
                "&MORE_FEEDS_CGI=http%3A%2F%2Fic2.qzone.qq.com%2Fcgi-bin%2Ffeeds%2Ffeeds_html_act_all&refer=2&paramstring=os-winxp|100";

        List<MyFriend> friends = getFriends();

        System.out.println("空间访问开始...");

        AtomicInteger i = new AtomicInteger(1);
        int total = friends.size();

        friends.forEach(friend->{
            //随机睡眠
            try {
                Thread.sleep(500+(int)(Math.random()*200));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            i.getAndIncrement();
            double progress = ((double) i.get() / total) * 100;
            System.out.printf("进度: %.2f%%\n", progress);
            String uin = friend.getUin();
            String zoneUrl = MessageFormat.format(url, uin);

            boolean exception = false;


            //空间访问
            try {

                // 使用jsoup连接网站并获取页面内容
                Document doc = Jsoup.connect(zoneUrl).cookie("cookie", cookie).get();
                ZoneException zoneException = new ZoneException();
                if(doc.body().toString().contains("未开通")){
                    exception = true;
                    zoneException.setReason("空间关闭");

                }else if(doc.body().toString().contains("没有权限")){
                    exception = true;
                    zoneException.setReason("无访问权限");

                }else{
                    Elements select = doc.select(".f-single.f-s-s");

                    for (Element element : select) {
                        Result result = new Result();
                        //qq
                        result.setQqNum(uin);
                        //时间
                        Elements select1 = element.select(".ui-mr8.state");
                        if(!select1.isEmpty()){
                            result.setTime(select1.get(0).html());
                        }

                        //设备
                        Elements select2 = element.select(".phone-style.state");
                        if(!select2.isEmpty()){
                            result.setDevice(select2.get(0).html());
                        }

                        //内容
                        result.setContent(filterChinese(element.select(".f-info").html()));



                        //查询是否存在
                        if(!resultMapper.exists(new QueryWrapper<Result>()
                                .eq("content",result.getContent())
                                .eq("qq_num",result.getQqNum())
                                .eq("time",result.getTime())
                        )){
                            resultMapper.insert(result);
                        }


                    }

                }

                if(exception){
                    zoneException.setQqNum(uin);
                    zoneException.setRemark(friend.getRemark());
                    //判断是否存在
                    if(!zoneExceptionMapper.exists(new QueryWrapper<ZoneException>()
                            .eq("qq_num",zoneException.getQqNum())
                    )){
                        zoneExceptionMapper.insert(zoneException);

                    }
                }




            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        System.out.println("空间访问结束");


    }

    public static String filterChinese(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            // 检查字符是否在汉字的Unicode范围内
            if ((c >= '\u4e00' && c <= '\u9fff') || // 基本汉字
                    (c >= '\u3400' && c <= '\u4dbf') || // CJK扩展A区
                    // 可以根据需要添加更多的Unicode范围
                    (Character.toString(c).matches("[\\p{InCJK_Unified_Ideographs}+\\p{InCJK_Compatibility_Ideographs}+\\p{InCJK_Compatibility_Forms}+\\p{InCJK_Symbols_And_Punctuation}+\\p{InEnclosed_CJK_Letters_And_Months}+\\p{InCJK_Radicals_Supplement}+\\p{InKangxi_Radicals}+\\p{InIdeographic_Description_Characters}+\\p{InCJK_Strokes}]+"))) {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    //说说数量统计
    public void countSS(){

        List<MyFriend> friends = getFriends();

        System.out.println("说说数量统计开始...");
        AtomicInteger i = new AtomicInteger(1);
        int total = friends.size();

        String url = "https://user.qzone.qq.com/proxy/domain/r.qzone.qq.com/cgi-bin/main_page_cgi?uin=" +
                "{0}" +
                "&param=3_2524358408_0%7C8_8_"+qqNum+"_1_1_0_0_1%7C15%7C16" +
                "&g_tk=" +g_tk+
                "&g_tk="+g_tk;

        for (MyFriend friend : friends) {

            i.getAndIncrement();
            double progress = ((double) i.get() / total) * 100;
            System.out.printf("说说数量统计进度: %.2f%%\n", progress);

            //随机睡眠
            try {
                Thread.sleep(500+(int)(Math.random()*200));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            SsXcRz ssXcRz = new SsXcRz();

            String format = MessageFormat.format(url, friend.getUin());

            try {
                HttpResponse execute = HttpUtil.createRequest(Method.GET, format).cookie(cookie).execute();

                //去除callback字符
                String substring = execute.body().substring(10);
                String substring1 = substring.substring(0, substring.length() - 2);
                Response data = JSONUtil.toBean(substring1, Response.class);
                if(data.getData()!=null){
                   if(data.getData().getModule_16()!=null){
                       if(data.getData().getModule_16().getData()!=null){
                           ssXcRz.setQqNum(friend.getUin());
                           ssXcRz.setRemark(friend.getRemark());

                           ssXcRz.setSs(Integer.parseInt(data.getData().getModule_16().getData().getSS()));
                           ssXcRz.setXc(Integer.parseInt(data.getData().getModule_16().getData().getXC()));
                           ssXcRz.setRz(Integer.parseInt(data.getData().getModule_16().getData().getRZ()));

                           if(!ssXcRzMapper.exists(new QueryWrapper<SsXcRz>().eq("qq_num",friend.getUin()))){
                               ssXcRzMapper.insert(ssXcRz);
                           }

                       }
                   }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        System.out.println("说说数量统计结束...");


    }

    //全部说说接口,json，非html
    public void visitZoneAll(){

    }




}

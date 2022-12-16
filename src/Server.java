import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Server {
    static SQLHandler sqlHandler;

    public static void main(String[] arg) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        sqlHandler = new SQLHandler();
        server.createContext("/api/db", new TestHandler());
        server.start();
    }

    static class TestHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) {
            new Thread(() -> {
                try{

                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin","*");
                    //只用POST
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Headers","content-type");


                    if(exchange.getRequestMethod().contentEquals("OPTIONS")) {
                        exchange.sendResponseHeaders(200,0);
                    } else {
                        byte[] requestBody = exchange.getRequestBody().readAllBytes();
                        System.out.println(new String(requestBody));
                        JSONObject jsonObject = JSON.parseObject(new String(requestBody));

                        switch (jsonObject.getString("header")) {
                            case "Login" -> {
                                JSONObject loginInfo = jsonObject.getJSONObject("user");
                                JSONObject responseJSON = new JSONObject();

                                if (sqlHandler.loginCheck(loginInfo.getString("name"), loginInfo.getString("passwd"))) {
                                    responseJSON.put("result",true);
                                } else {
                                    responseJSON.put("result",false);
                                }
                                byte[] response = JSON.toJSONBytes(responseJSON);
                                exchange.sendResponseHeaders(200,response.length);
                                exchange.getResponseBody().write(response);
                                exchange.getResponseBody().close();
                            }
                        }
                    }

                    //获得查询字符串(get)
//                        String queryString =  exchange.getRequestURI().getQuery();
//                        Map<String,String> queryStringInfo = formData2Dic(queryString);
                    //获得表单提交数据(post)

//                        String postString = IOUtils.toString(exchange.getRequestBody());
//                        Map<String,String> postInfo = formData2Dic(postString);


//                        OutputStream os = exchange.getResponseBody();
//                        os.write(response.getBytes());
//                        os.close();
                }catch (IOException ie) {
                    ie.printStackTrace();
                }
            }).start();
        }
    }

//    public static Map<String,String> formData2Dic(String formData ) {
//        Map<String,String> result = new HashMap<>();
//        if(formData== null || formData.trim().length() == 0) {
//            return result;
//        }
//        final String[] items = formData.split("&");
//        Arrays.stream(items).forEach(item ->{
//            final String[] keyAndVal = item.split("=");
//            if( keyAndVal.length == 2) {
//                try{
//                    final String key = URLDecoder.decode( keyAndVal[0],"utf8");
//                    final String val = URLDecoder.decode( keyAndVal[1],"utf8");
//                    result.put(key,val);
//                }catch (UnsupportedEncodingException e) {}
//            }
//        });
//        return result;
//    }
}
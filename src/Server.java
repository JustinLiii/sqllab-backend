import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

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


                    System.out.println("New exchange " + exchange.getRequestMethod());


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

                                boolean[] result = sqlHandler.loginCheck(loginInfo.getString("name"), loginInfo.getString("passwd"));
                                if (result[0]) {
                                    responseJSON.put("result",true);
                                } else {
                                    responseJSON.put("result",false);
                                }
                                if (result[1]) {
                                    responseJSON.put("admin",true);
                                } else {
                                    responseJSON.put("admin",false);
                                }
                                sendResponse(exchange,responseJSON);
                            }
                            case "CheckSignUp" -> {
                                JSONObject checkInfo = jsonObject.getJSONObject("user");
                                JSONObject responseJSON = new JSONObject();

                                if (sqlHandler.checkSignUp(checkInfo.getString("name"))) {
                                    responseJSON.put("result",true);
                                } else {
                                    responseJSON.put("result",false);
                                }
                                sendResponse(exchange,responseJSON);
                            }
                            case "SignUp" -> {
                                JSONObject signUpInfo = jsonObject.getJSONObject("user");
                                JSONObject responseJSON = new JSONObject();
                                sqlHandler.signUp(signUpInfo.getString("name"),signUpInfo.getString("passwd"),signUpInfo.getBoolean("admin"));
                                responseJSON.put("result",true);
                                sendResponse(exchange,responseJSON);
                            }
                            case "CreatedActivity" -> {
                                JSONObject userInfo = jsonObject.getJSONObject("user");
                                JSONObject responseJSON = new JSONObject();

                                if (!userInfo.getBoolean("admin")) {
                                    responseJSON.put("result",false);
                                } else {
                                    responseJSON.put("result", true);
                                    responseJSON.put("activities",new JSONArray(sqlHandler.getList(userInfo.getString("name"), SQLHandler.ActivityType.Creator)));
                                }
                                sendResponse(exchange,responseJSON);
                            }
                            case "AllActivity" -> {
                                JSONObject responseJSON = new JSONObject();
                                responseJSON.put("result", true);
                                responseJSON.put("activities",new JSONArray(sqlHandler.getList(null, SQLHandler.ActivityType.All)));
                                sendResponse(exchange,responseJSON);
                            }
                            case "Apply" -> {
                                JSONObject responseJSON = new JSONObject();
                                JSONObject applyInfo = jsonObject.getJSONObject("apply");
                                sqlHandler.apply(applyInfo.getInteger("id"),applyInfo.getString("user"));
                                responseJSON.put("result", true);
                                sendResponse(exchange,responseJSON);
                            }
                            case "AppliedActivity" -> {
                                JSONObject responseJSON = new JSONObject();
                                responseJSON.put("result", true);
                                responseJSON.put("activities",new JSONArray(sqlHandler.getList(jsonObject.getString("user"), SQLHandler.ActivityType.Applied)));
                                sendResponse(exchange,responseJSON);
                            }
                            case "NewActivity" -> {
                                JSONObject responseJSON = new JSONObject();
                                sqlHandler.createActivity(jsonObject.getJSONObject("info"));
                                responseJSON.put("result", true);
                                sendResponse(exchange,responseJSON);
                            }
                            case "DropActivity" -> {
                                JSONObject responseJSON = new JSONObject();
                                sqlHandler.dropActivity(jsonObject.getInteger("id"));
                                responseJSON.put("result", true);
                                sendResponse(exchange,responseJSON);
                            }
                            case "GetApplicants" -> {
                                JSONObject responseJSON = new JSONObject();
                                responseJSON.put("result", true);
                                responseJSON.put("applicants",new JSONArray(sqlHandler.getApplicants(jsonObject.getInteger("id"))));
                                sendResponse(exchange,responseJSON);
                            }
                            case "Agree" -> {
                                JSONObject responseJSON = new JSONObject();
                                sqlHandler.agree(jsonObject.getJSONObject("data").getString("name"),jsonObject.getJSONObject("data").getInteger("id"),jsonObject.getJSONObject("data").getBoolean("agree"));
                                responseJSON.put("result", true);
                                sendResponse(exchange,responseJSON);
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

    static protected void sendResponse(HttpExchange exchange,JSONObject responseJSON) throws IOException {
        byte[] response = JSON.toJSONBytes(responseJSON);
        exchange.sendResponseHeaders(200,response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
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
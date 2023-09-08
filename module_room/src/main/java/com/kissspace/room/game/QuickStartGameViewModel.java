package com.kissspace.room.game;

import static com.blankj.utilcode.util.DeviceUtils.getAndroidID;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.github.gzuliyujiang.oaid.DeviceIdentifier;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kissspace.common.config.BaseUrlConfig;
import com.kissspace.common.config.Constants;
import com.kissspace.common.http.interceptor.HeaderInterceptor;
import com.kissspace.common.model.immessage.BaseAttachment;
import com.kissspace.common.model.immessage.RoomChatMessageModel;
import com.kissspace.common.util.mmkv.MMKVProvider;
import com.kissspace.room.game.bean.RoomGameKeyWordBean;
import com.kissspace.room.game.bean.RoomGameSelfTextBean;
import com.kissspace.room.game.bean.RoomGameStateBean;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSMMGListener;
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSTAPPDecorator;
import tech.sud.mgp.SudMGPWrapper.model.GameConfigModel;
import tech.sud.mgp.SudMGPWrapper.model.GameViewInfoModel;
import tech.sud.mgp.SudMGPWrapper.state.MGStateResponse;
import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState;
import tech.sud.mgp.core.ISudFSMMG;
import tech.sud.mgp.core.ISudFSMStateHandle;

/**
 * 游戏业务逻辑
 * 1.自定义ViewModel继承此类，实现对应方法。(注意：onAddGameView()与onRemoveGameView()与页面有交互)
 * 2.外部调用switchGame(activity,gameRoomId,gameId)方法启动游戏，参数定义可查看方法注释。
 * 3.页面销毁时调用onDestroy()
 */
public class QuickStartGameViewModel extends BaseGameViewModel {

    /**
     * Sud平台申请的appId
     */
    public static String SudMGP_APP_ID = "1673968199178969089";
    /**
     * Sud平台申请的appKey
     */
    public static String SudMGP_APP_KEY = "i1nAEpPRYMc5nFgvvSQSP8H4NR09qVfj";
    /**
     * true 加载游戏时为测试环境 false 加载游戏时为生产环境
     */
    public static final boolean GAME_IS_TEST_ENV = false;


    public static final String COMMON_PLAYER_PALAYING = "mg_common_player_playing";

    public static final String MG_COMMON_GAME_STATE = "mg_common_game_state";

    public static final String MG_COMMON_PUBLIC_MESSAGE = "mg_common_public_message";

    public static final String MG_COMMON_KEY_WORD_TO_HIT = "mg_common_key_word_to_hit";

    /**
     * 使用的UserId。这里随机生成作演示，开发者将其修改为业务使用的唯一userId
     */
    public static String userId = MMKVProvider.INSTANCE.getUserId();

    /**
     * 游戏自定义安全操作区域
     */
    public GameViewInfoModel.GameViewRectModel gameViewRectModel;

    /**
     * 游戏的语言代码
     */
    public String languageCode = "zh-CN";

    public final MutableLiveData<View> gameViewLiveData = new MutableLiveData<>(); // 游戏View回调


    private RoomGameListener mRoomGameListener;

    private String keyWord = "";

    public String name = "";

    /**
     * 向接入方服务器获取code
     */
    @Override
    protected void getCode(Activity activity, String userId, String appId, GameGetCodeListener listener) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        String req;
        try {
            JSONObject reqJsonObj = new JSONObject();
            reqJsonObj.put("user_id", userId);
            req = reqJsonObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
            req = "";
        }

        RequestBody body = RequestBody.create(req, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(BaseUrlConfig.BASEURL_RELEASE+"/djsoul-game/sudMGP/login")
                .post(body)
                .addHeader("Authorization", MMKVProvider.INSTANCE.getLoginResult().getTokenHead() + MMKVProvider.INSTANCE.getLoginResult().getToken())
                .addHeader("channel", appendChannel(activity))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFailed();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String dataJson = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(dataJson);
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    String responseCode = jsonObject.getString("responseCode");
                    String code = dataObject.getString("code");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (responseCode.equals("200")) {
                                listener.onSuccess(code);
                            } else {
                                listener.onFailed();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailed();
                        }
                    });
                }
            }
        });
    }

    public void setOnRoomGameListener(RoomGameListener listener){
        mRoomGameListener = listener;
    }

    private String appendChannel(Activity activity) {
        JSONObject json = new JSONObject();
        try {
            json.put("deviceId", DeviceUtils.getUniqueDeviceId());
            json.put("channelCode", "djs");
            json.put("idfa", "");
            json.put("oaid", DeviceIdentifier.getOAID(activity));
            json.put("androidId", getAndroidID());
            json.put("imei", "");
            json.put("mac", "");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return json.toString();
    }

    /**
     * 设置当前用户id(接入方定义)
     */
    @Override
    protected String getUserId() {
        return userId;
    }

    /**
     * 设置Sud平台申请的appId
     */
    @Override
    protected String getAppId() {
        return SudMGP_APP_ID;
    }

    /**
     * 设置Sud平台申请的appKey
     */
    @Override
    protected String getAppKey() {
        return SudMGP_APP_KEY;
    }

    /**
     * 设置游戏的语言代码
     */
    @Override
    protected String getLanguageCode() {
        return languageCode;
    }

    /**
     * 设置游戏的安全操作区域，{@link ISudFSMMG}.onGetGameViewInfo()的实现。
     * 参考文档：https://docs.sud.tech/zh-CN/app/Client/API/ISudFSMMG/onGetGameViewInfo.html
     *
     * @param gameViewInfoModel 游戏视图模型
     */
    @Override
    protected void getGameRect(GameViewInfoModel gameViewInfoModel) {
        // 相对于view_size（左、上、右、下）边框偏移（单位像素）
        // 开发者可自定义gameViewRectModel来控制安全区域
        if (gameViewRectModel != null) {
            gameViewInfoModel.view_game_rect = gameViewRectModel;
        }
    }

    /**
     * 获取游戏配置对象，{@link ISudFSMMG}.onGetGameCfg()的实现。
     * 参考文档：https://docs.sud.tech/zh-CN/app/Client/API/ISudFSMMG/onGetGameCfg.html
     * 开发者拿到此对象之后，可修改自己需要的配置
     * 注意：在加载游戏之前配置才有效
     *
     * @return 游戏配置对象
     */
    public GameConfigModel getGameConfigModel() {
        return gameConfigModel;
    }

    /**
     * true 加载游戏时为测试环境
     * false 加载游戏时为生产环境
     */
    @Override
    protected boolean isTestEnv() {
        return GAME_IS_TEST_ENV;
    }

    /**
     * 将游戏View添加到页面中
     */
    @Override
    protected void onAddGameView(View gameView) {
        gameViewLiveData.setValue(gameView);
    }

    /**
     * 将页面中的游戏View移除
     */
    @Override
    protected void onRemoveGameView() {
        gameViewLiveData.setValue(null);
    }

    // ************ 上面是基础能力以及必要配置，下面讲解状态交互
    // ************ 主要有：1.App向游戏发送状态；2.游戏向App回调状态

    /**
     * 1.App向游戏发送状态
     * 这里演示的是发送：1. 加入状态；
     * 开发者可自由定义方法，能发送的状态都封装在{@link SudFSTAPPDecorator}
     * 参考文档：https://docs.sud.tech/zh-CN/app/Client/APPFST/
     * 注意：
     * 1，App向游戏发送状态，因为需要走网络，所以向游戏发送状态之后，不能马上销毁游戏或者finish Activity，否则状态无法发送成功。
     * 2，要保证状态能到达，可以发送之后，delay 500ms再销毁游戏或者finish Activity。
     */
    public void notifyAPPCommonSelfIn(boolean isIn, int seatIndex, boolean isSeatRandom, int teamId) {
        sudFSTAPPDecorator.notifyAPPCommonSelfIn(isIn, seatIndex, isSeatRandom, teamId);
    }

    /**
     * 2.游戏向App回调状态
     * 这里演示的是接收游戏回调状态：10. 游戏状态 mg_common_game_state
     * 游戏回调的每个状态都对应着一个方法，方法定义在：{@link SudFSMMGListener}
     */
    @Override
    public void onGameMGCommonGameState(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameState model) {
        super.onGameMGCommonGameState(handle, model);
    }


    @Override
    public boolean onGameStateChange(ISudFSMStateHandle handle, String state, String dataJson) {
        switch (state){
            case MG_COMMON_GAME_STATE:
                RoomGameStateBean roomGameStateBean = GsonUtils.fromJson(dataJson, RoomGameStateBean.class);
                if (mRoomGameListener != null){
                    mRoomGameListener.onGameState(roomGameStateBean.gameState == 2);
                }
                break;
            case MG_COMMON_PUBLIC_MESSAGE:
                SudMGPMGState.MGCommonPublicMessage publicMessage = GsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPublicMessage.class);
                if (publicMessage.type == 0){
                    String message = "";
                    for (SudMGPMGState.MGCommonPublicMessage.MGCommonPublicMessageMsg mgCommonPublicMessageMsg : publicMessage.msg) {
                        if (mgCommonPublicMessageMsg.phrase == 1){
                            message += mgCommonPublicMessageMsg.text.zh_CN;
                        }else {
                            message += mgCommonPublicMessageMsg.user.name;
                        }
                    }
                    if (mRoomGameListener != null){
                        mRoomGameListener.onGameMessage(message);
                    }
                }
                break;
            case MG_COMMON_KEY_WORD_TO_HIT:
                RoomGameKeyWordBean roomGameKeyWordBean = GsonUtils.fromJson(dataJson, RoomGameKeyWordBean.class);
                if (mRoomGameListener != null){
                    mRoomGameListener.onKeyWordInputState(!TextUtils.isEmpty(roomGameKeyWordBean.word) && !roomGameKeyWordBean.word.equals("null"));
                }
                keyWord = roomGameKeyWordBean.word;
                break;
        }
        return super.onGameStateChange(handle, state, dataJson);
    }

    /**
     * 发送文本消息
     * @param roomChatId 聊天室Id
     * @param content 发送内容
     */
    public void  sendTextMessage(
            String roomChatId,
            String content
            ) {

        if (content.equals(keyWord)){
            RoomGameSelfTextBean roomGameSelfTextBean = new RoomGameSelfTextBean();
            roomGameSelfTextBean.isHit = true;
            roomGameSelfTextBean.keyWord = keyWord;
            roomGameSelfTextBean.text = content;
            sudFSTAPPDecorator.notifyStateChange("app_common_self_text_hit",GsonUtils.toJson(roomGameSelfTextBean));
        }

        RoomChatMessageModel model = new RoomChatMessageModel();
        model.setContent(content);
        model.setName(name);
        model.setNickname(name);
        BaseAttachment attr = new BaseAttachment(Constants.IMMessageType.MSG_TYPE_ROOM_CHAT_GAME, model);
        ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomChatId, attr);
        message.setContent(content);
        NIMClient.getService(ChatRoomService.class).sendMessage(message, false);
    }

}

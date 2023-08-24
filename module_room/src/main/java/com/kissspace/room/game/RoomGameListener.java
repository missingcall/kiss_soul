package com.kissspace.room.game;

import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState;

/**
 * @author: adan
 * @date: 2023/7/10
 * @Description:
 */
public interface RoomGameListener {
    void onGameState(boolean isStart);

    void onGameMessage(String message);

    void onKeyWordInputState(boolean isShow);
}

package Project.common;

/*
* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
*   - Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
*
*   - Redistributions in binary form must reproduce the above copyright
*     notice, this list of conditions and the following disclaimer in the
*     documentation and/or other materials provided with the distribution.
*
*   - Neither the name of Oracle or the names of its
*     contributors may be used to endorse or promote products derived
*     from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
* IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
* THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
* PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Simple countdown timer demo of java.util.Timer facility.
 */

public class Countdown {
    private int time;
    private String message;
    private Runnable expireCallback = null;
    private Consumer<Integer> tickCallback = null;
    final private Timer timer;
    private static MyLogger logger = MyLogger.getLogger(Countdown.class.getName());

    public Countdown(String message, int duration, Runnable callback) {
        this(message, duration);
        this.expireCallback = callback;
    }
    public Countdown(String message, int duration) {
        timer = new Timer();
        this.message = message;
        time = duration;
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                time--;
                logger.info("Time: " + time);
                if(tickCallback != null){
                    tickCallback.accept(time);
                }
                if (time <= 0) {
                    timer.cancel();
                    time = 0;
                    if (expireCallback != null) {
                        expireCallback.run();
                    }
                }
            }
        }, 1000, 1000);
    }
    public void setTickCallback(Consumer<Integer> callback){
        tickCallback = callback;
    }
    public void setExpireCallback(Runnable callback){
        expireCallback = callback;
    }
    public void cancel() {
        expireCallback = null;
        timer.cancel();
    }

    public String getTimeMessage() {
        if (message == null) {
            return "";
        }
        if (time == 0) {
            return String.format("%s: %s", message, "expired");
        }
        return String.format("%s: %s", message, time);
    }
}
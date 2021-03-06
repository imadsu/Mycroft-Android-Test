/*
 *  Copyright (c) 2017. Mycroft AI, Inc.
 *
 *  This file is part of Mycroft-Android a client for Mycroft Core.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package mycroft.ai

import android.util.Log

import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Specialised Runnable that parses the [JSONObject] in [.message]
 * when run. If it contains a [Utterance] object, the callback
 * defined in [the constructor][.MessageParser] will
 * be [called][SafeCallback.call] with that object as a parameter.
 *
 *
 * TODO: Add error-aware callback for cases where the message is malformed.
 *
 *
 * @author Philip Cohn-Cort
 */
var userSaidNormal = false;
internal class MessageParser(private val message: String,
                             private val callback: SafeCallback<Utterance>) : Runnable {
    private val logTag = "MessageParser"


    override fun run() {
        Log.i(logTag, message)
        // new format
        // {"data": {"utterance": "There are only two hard problems in Computer Science: cache invalidation, naming things and off-by-one-errors."}, "type": "speak", "context": null}
        try {
            val obj = JSONObject(message)


            // only happens when Mycroft speaks
            //if (obj.optString("type") == "recognizer_loop:utterance") {
                //print("hello there")
            val userSaid = Utterance(obj.getJSONObject("data").getString("utterance"), UtteranceFrom.USER)
            if(userSaid.utterance.toLowerCase() == "normal"){
                userSaidNormal = true;
            }
            //}
            if (obj.optString("type") == "speak") {
                //"<speak><prosody volume= " + settings_dict["volume"]
                //                                    + " rate= " + settings_dict["rate"] + ">" +
                //                                    chunk + "</prosody></speak>"
                //val obj = JSONObject("{\"type\": \"speak\", \"data\": {\"utterance\": \"<speak><prosody rate='0.6'>I change it! Please work oh god please alright I am done now</prosody></speak>\", \"expect_response\": false}, \"context\": {}}");
                var ret = Utterance(obj.getJSONObject("data").getString("utterance"), UtteranceFrom.MYCROFT)

                if(userSaidNormal) {
                    ret = Utterance("settings are now normal", UtteranceFrom.MYCROFT)
                    userSaidNormal = false;
                }

                callback.call(ret)
            }

        } catch (e: JSONException) {
            Log.e(logTag, "The response received did not conform to our expected JSON format.", e)
        }
    }
}

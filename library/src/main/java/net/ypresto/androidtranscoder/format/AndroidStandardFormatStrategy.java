/*
 * Copyright (C) 2016 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ypresto.androidtranscoder.format;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

public class AndroidStandardFormatStrategy implements MediaFormatStrategy {
    
    private static final String TAG = "StandardSizesFormat";

    // these are the codecs that android says will work on all devices:
    // https://developer.android.com/guide/appendix/media-formats.html
    public enum Encoding {
        SD_LOW(176, 144, 56, 12, 1, 24), 
        SD_HIGH(480, 360, 500, 30, 2, 128), 
        HD_720P(1280, 720, 2000, 30, 2, 192);

        public int longerLength;
        public int shorterLength;
        public int bitrate;
        public int frameRate;
        public int audioChannels;
        public int audioBitrate;
        
        private Encoding(int longerLength, int shorterLength, int bitrate, int frameRate, int audioChannels, int audioBitrate) {
            this.longerLength = longerLength;
            this.shorterLength = shorterLength;
            this.bitrate = bitrate * 1000;
            this.frameRate = frameRate;
            this.audioChannels = audioChannels;
            this.audioBitrate = audioBitrate * 1000;
        }
    }

    private final int longerLength;
    private final int shorterLength;
    private final int videoBitrate;
    private final int frameRate;
    private final int audioChannels;
    private final int audioBitrate;

    public AndroidStandardFormatStrategy(Encoding encoding) {
        this.longerLength = encoding.longerLength;
        this.shorterLength = encoding.shorterLength;
        this.videoBitrate = encoding.bitrate;
        this.frameRate = encoding.frameRate;
        this.audioChannels = encoding.audioChannels;
        this.audioBitrate = encoding.audioBitrate;
    }

    @Override
    public MediaFormat createVideoOutputFormat(MediaFormat inputFormat) {
        int width = inputFormat.getInteger(MediaFormat.KEY_WIDTH);
        int height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT);
        int longer, shorter, outWidth, outHeight;

        if (width >= height) {
            longer = width;
            shorter = height;
            outWidth = longerLength;
            outHeight = shorterLength;
        } else {
            shorter = width;
            longer = height;
            outWidth = shorterLength;
            outHeight = longerLength;
        }

        if (longer * 9 != shorter * 16) {
            throw new OutputFormatUnavailableException("This video is not 16:9, and is not able to transcode. (" + width + "x" + height + ")");
        }

        if (shorter <= shorterLength) {
            Log.d(TAG, "This video is less or equal to the specified format, pass-through. (" + width + "x" + height + ")");
            return null;
        }

        MediaFormat format = MediaFormat.createVideoFormat("video/avc", outWidth, outHeight);
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

        return format;
    }

    @Override
    public MediaFormat createAudioOutputFormat(MediaFormat inputFormat) {
        final MediaFormat format = MediaFormat.createAudioFormat(MediaFormatExtraConstants.MIMETYPE_AUDIO_AAC,
                inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE), audioChannels);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, audioBitrate);

        return format;
    }
}

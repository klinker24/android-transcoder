package net.ypresto.androidtranscoder.format;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

public class AspectRatioFormatStrategy implements MediaFormatStrategy {

    private static final String TAG = "StandardSizesFormat";

    private final int shorterLength;
    private final int videoBitrate;
    private final int frameRate;
    private final int audioChannels;
    private final int audioBitrate;

    public AspectRatioFormatStrategy(AndroidStandardFormatStrategy.Encoding encoding) {
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
            outWidth = (height / shorterLength) * width;
            outHeight = shorterLength;
        } else {
            shorter = width;
            longer = height;
            outWidth = shorterLength;
            outHeight = (width / shorterLength) * height;
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
                inputFormat != null ?inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) : 1, audioChannels);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, audioBitrate);

        return format;
    }
}

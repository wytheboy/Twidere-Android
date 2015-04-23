package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonWriter;

import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.twidere.model.iface.JsonReadable;
import org.mariotaku.twidere.model.iface.JsonWritable;
import org.mariotaku.twidere.util.JsonSerializationUtils;
import org.mariotaku.twidere.util.MediaPreviewUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.TwidereArrayUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twitter4j.CardEntity;
import twitter4j.CardEntity.BindingValue;
import twitter4j.CardEntity.ImageValue;
import twitter4j.CardEntity.StringValue;
import twitter4j.EntitySupport;
import twitter4j.ExtendedEntitySupport;
import twitter4j.MediaEntity;
import twitter4j.MediaEntity.Size;
import twitter4j.MediaEntity.Type;
import twitter4j.Status;
import twitter4j.URLEntity;

@SuppressWarnings("unused")
public class ParcelableMedia implements Parcelable, JSONParcelable, JsonReadable, JsonWritable {

    public static ParcelableMedia[] fromSerializedJson(String string) {
        if (TextUtils.isEmpty(string)) return null;
        JsonReader reader = new JsonReader(new StringReader(string));
        try {
            reader.beginArray();
            final ParcelableMedia[] media = JsonSerializationUtils.array(reader, ParcelableMedia.class);
            reader.endArray();
            return media;
        } catch (IOException ignore) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException ignore) {
            }
        }
    }

    @Override
    public void read(JsonReader reader) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "media_url": {
                    media_url = reader.nextString();
                    break;
                }
                case "page_url": {
                    page_url = reader.nextString();
                    break;
                }
                case "preview_url": {
                    preview_url = reader.nextString();
                    break;
                }
                case "start": {
                    start = reader.nextInt();
                    break;
                }
                case "end": {
                    end = reader.nextInt();
                    break;
                }
                case "type": {
                    //noinspection ResourceType
                    type = reader.nextInt();
                    break;
                }
                case "width": {
                    width = reader.nextInt();
                    break;
                }
                case "height": {
                    height = reader.nextInt();
                    break;
                }
                case "video_info": {
                    reader.beginObject();
                    video_info = JsonSerializationUtils.object(reader, VideoInfo.class);
                    reader.endObject();
                    break;
                }
                default: {
                    reader.skipValue();
                    break;
                }
            }
        }
        reader.endObject();
    }

    @IntDef({TYPE_UNKNOWN, TYPE_IMAGE, TYPE_VIDEO, TYPE_ANIMATED_GIF, TYPE_CARD_ANIMATED_GIF})
    public @interface MediaType {

    }

    @MediaType
    public static final int TYPE_UNKNOWN = 0;
    @MediaType
    public static final int TYPE_IMAGE = 1;
    @MediaType
    public static final int TYPE_VIDEO = 2;
    @MediaType
    public static final int TYPE_ANIMATED_GIF = 3;
    @MediaType
    public static final int TYPE_CARD_ANIMATED_GIF = 4;

    public static final Parcelable.Creator<ParcelableMedia> CREATOR = new Parcelable.Creator<ParcelableMedia>() {
        @Override
        public ParcelableMedia createFromParcel(final Parcel in) {
            return new ParcelableMedia(in);
        }

        @Override
        public ParcelableMedia[] newArray(final int size) {
            return new ParcelableMedia[size];
        }
    };

    public static final JSONParcelable.Creator<ParcelableMedia> JSON_CREATOR = new JSONParcelable.Creator<ParcelableMedia>() {
        @Override
        public ParcelableMedia createFromParcel(final JSONParcel in) {
            return new ParcelableMedia(in);
        }

        @Override
        public ParcelableMedia[] newArray(final int size) {
            return new ParcelableMedia[size];
        }
    };


    @NonNull
    public String media_url;

    @Nullable
    public String page_url, preview_url;
    public int start;
    public int end;

    @MediaType
    public int type;
    public int width, height;

    public VideoInfo video_info;


    public ParcelableMedia() {

    }


    public ParcelableMedia(final JSONParcel in) {
        media_url = in.readString("media_url");
        page_url = in.readString("page_url");
        preview_url = in.readString("preview_url");
        start = in.readInt("start");
        end = in.readInt("end");
        //noinspection ResourceType
        type = in.readInt("type");
        width = in.readInt("width");
        height = in.readInt("height");
    }

    public ParcelableMedia(final MediaEntity entity) {
        page_url = entity.getMediaURL();
        media_url = entity.getMediaURL();
        preview_url = entity.getMediaURL();
        start = entity.getStart();
        end = entity.getEnd();
        type = getTypeInt(entity.getType());
        final Size size = entity.getSizes().get(Size.LARGE);
        width = size != null ? size.getWidth() : 0;
        height = size != null ? size.getHeight() : 0;
        video_info = VideoInfo.fromMediaEntityInfo(entity.getVideoInfo());
    }

    public ParcelableMedia(ParcelableMediaUpdate update) {
        media_url = update.uri;
        page_url = update.uri;
        preview_url = update.uri;
        type = update.type;
    }

    @Nullable
    public static ParcelableMedia[] fromMediaUpdates(@Nullable final ParcelableMediaUpdate[] mediaUpdates) {
        if (mediaUpdates == null) return null;
        final ParcelableMedia[] media = new ParcelableMedia[mediaUpdates.length];
        for (int i = 0, j = mediaUpdates.length; i < j; i++) {
            final ParcelableMediaUpdate mediaUpdate = mediaUpdates[i];
            media[i] = new ParcelableMedia(mediaUpdate);
        }
        return media;
    }

    private static int getTypeInt(Type type) {
        switch (type) {
            case PHOTO:
                return TYPE_IMAGE;
            case VIDEO:
                return TYPE_VIDEO;
            case ANIMATED_GIF:
                return TYPE_ANIMATED_GIF;
        }
        return TYPE_UNKNOWN;
    }

    public ParcelableMedia(final Parcel in) {
        page_url = in.readString();
        media_url = in.readString();
        preview_url = in.readString();
        start = in.readInt();
        end = in.readInt();
        //noinspection ResourceType
        type = in.readInt();
        width = in.readInt();
        height = in.readInt();
        video_info = in.readParcelable(VideoInfo.class.getClassLoader());
    }

    private ParcelableMedia(@NonNull final String media_url, @Nullable final String page_url,
                            @Nullable final String preview_url, final int start, final int end,
                            final int type) {
        this.page_url = page_url;
        this.media_url = media_url;
        this.preview_url = preview_url;
        this.start = start;
        this.end = end;
        this.type = type;
        this.width = 0;
        this.height = 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableMedia that = (ParcelableMedia) o;

        if (end != that.end) return false;
        if (start != that.start) return false;
        if (type != that.type) return false;
        if (!media_url.equals(that.media_url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = media_url.hashCode();
        result = 31 * result + start;
        result = 31 * result + end;
        result = 31 * result + type;
        return result;
    }


    @Override
    public String toString() {
        return "ParcelableMedia{" +
                "media_url='" + media_url + '\'' +
                ", page_url='" + page_url + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", type=" + type +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

    @Override
    public void write(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("media_url").value(media_url);
        writer.name("page_url").value(page_url);
        writer.name("preview_url").value(preview_url);
        writer.name("start").value(start);
        writer.name("end").value(end);
        writer.name("type").value(type);
        writer.name("width").value(width);
        writer.name("height").value(height);
        if (video_info != null) {
            writer.name("video_info");
            video_info.write(writer);
        }
        writer.endObject();
    }

    @Override
    public void writeToParcel(final JSONParcel out) {
        out.writeString("media_url", media_url);
        out.writeString("page_url", page_url);
        out.writeString("preview_url", preview_url);
        out.writeInt("start", start);
        out.writeInt("end", end);
        out.writeInt("type", type);
        out.writeInt("width", width);
        out.writeInt("height", height);
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(page_url);
        dest.writeString(media_url);
        dest.writeString(preview_url);
        dest.writeInt(start);
        dest.writeInt(end);
        dest.writeInt(type);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeParcelable(video_info, flags);
    }


    @Nullable
    public static ParcelableMedia[] fromStatus(final Status status) {
        final ParcelableMedia[] fromEntities = fromEntities(status);
        final ParcelableMedia[] fromCard = fromCard(status.getCard(), status.getURLEntities());
        if (fromEntities == null) {
            return fromCard;
        } else if (fromCard == null) {
            return fromEntities;
        }
        final ParcelableMedia[] merged = new ParcelableMedia[fromCard.length + fromEntities.length];
        TwidereArrayUtils.mergeArray(merged, fromEntities, fromCard);
        return merged;
    }

    @Nullable
    private static ParcelableMedia[] fromCard(@Nullable CardEntity card, @Nullable URLEntity[] entities) {
        if (card == null) return null;
        if ("animated_gif".equals(card.getName())) {
            final BindingValue player_stream_url = card.getBindingValue("player_stream_url");
            if (player_stream_url == null || !BindingValue.TYPE_STRING.equals(player_stream_url.getType()))
                return null;

            final ParcelableMedia media = new ParcelableMedia();
            media.type = ParcelableMedia.TYPE_CARD_ANIMATED_GIF;
            media.media_url = ((StringValue) player_stream_url).getValue();
            media.page_url = card.getUrl();
            final BindingValue player_image = card.getBindingValue("player_image");
            if (player_image instanceof ImageValue) {
                media.preview_url = ((ImageValue) player_image).getUrl();
            }
            final BindingValue player_width = card.getBindingValue("player_width");
            final BindingValue player_height = card.getBindingValue("player_height");
            if (player_width instanceof StringValue && player_height instanceof StringValue) {
                media.width = ParseUtils.parseInt(((StringValue) player_width).getValue());
                media.height = ParseUtils.parseInt(((StringValue) player_height).getValue());
            }
            if (entities != null) {
                for (URLEntity entity : entities) {
                    if (entity.getURL().equals(media.page_url)) {
                        media.start = entity.getStart();
                        media.end = entity.getEnd();
                        break;
                    }
                }
            }
            return new ParcelableMedia[]{media};
        }
        return null;
    }

    @Nullable
    public static ParcelableMedia[] fromEntities(@Nullable final EntitySupport entities) {
        if (entities == null) return null;
        final List<ParcelableMedia> list = new ArrayList<>();
        final MediaEntity[] mediaEntities;
        if (entities instanceof ExtendedEntitySupport) {
            final ExtendedEntitySupport extendedEntities = (ExtendedEntitySupport) entities;
            final MediaEntity[] extendedMediaEntities = extendedEntities.getExtendedMediaEntities();
            mediaEntities = extendedMediaEntities != null ? extendedMediaEntities : entities.getMediaEntities();
        } else {
            mediaEntities = entities.getMediaEntities();
        }
        if (mediaEntities != null) {
            for (final MediaEntity media : mediaEntities) {
                final String mediaURL = media.getMediaURL();
                if (mediaURL != null) {
                    list.add(new ParcelableMedia(media));
                }
            }
        }
        final URLEntity[] urlEntities = entities.getURLEntities();
        if (urlEntities != null) {
            for (final URLEntity url : urlEntities) {
                final String expanded = url.getExpandedURL();
                final String media_url = MediaPreviewUtils.getSupportedLink(expanded);
                if (expanded != null && media_url != null) {
                    final ParcelableMedia media = new ParcelableMedia();
                    media.type = TYPE_IMAGE;
                    media.page_url = expanded;
                    media.media_url = media_url;
                    media.preview_url = media_url;
                    media.start = url.getStart();
                    media.end = url.getEnd();
                    list.add(media);
                }
            }
        }
        if (list.isEmpty()) return null;
        return list.toArray(new ParcelableMedia[list.size()]);
    }


    public static class VideoInfo implements Parcelable, JsonReadable, JsonWritable {

        public Variant[] variants;
        public long duration;


        public VideoInfo() {

        }

        public VideoInfo(MediaEntity.VideoInfo videoInfo) {
            variants = Variant.fromMediaEntityVariants(videoInfo.getVariants());
            duration = videoInfo.getDuration();
        }

        public static VideoInfo fromMediaEntityInfo(MediaEntity.VideoInfo videoInfo) {
            if (videoInfo == null) return null;
            return new VideoInfo(videoInfo);
        }

        @Override
        public void read(JsonReader reader) throws IOException {
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "variants": {
                        reader.beginArray();
                        variants = JsonSerializationUtils.array(reader, Variant.class);
                        reader.endArray();
                        break;
                    }
                    case "duration": {
                        duration = reader.nextLong();
                        break;
                    }
                    default: {
                        reader.skipValue();
                        break;
                    }
                }
            }
            reader.endObject();
        }

        @Override
        public String toString() {
            return "VideoInfo{" +
                    "variants=" + Arrays.toString(variants) +
                    ", duration=" + duration +
                    '}';
        }

        @Override
        public void write(JsonWriter writer) {

        }

        public static class Variant implements Parcelable, JsonReadable {
            public Variant(MediaEntity.VideoInfo.Variant entityVariant) {
                content_type = entityVariant.getContentType();
                url = entityVariant.getUrl();
                bitrate = entityVariant.getBitrate();
            }

            @Override
            public void read(JsonReader reader) throws IOException {
                reader.beginObject();
                while (reader.hasNext()) {
                    switch (reader.nextName()) {
                        case "content_type": {
                            content_type = reader.nextString();
                            break;
                        }
                        case "url": {
                            url = reader.nextString();
                            break;
                        }
                        case "bitrate": {
                            bitrate = reader.nextLong();
                            break;
                        }
                        default: {
                            reader.skipValue();
                            break;
                        }
                    }
                }
                reader.endObject();
            }

            @Override
            public String toString() {
                return "Variant{" +
                        "content_type='" + content_type + '\'' +
                        ", url='" + url + '\'' +
                        ", bitrate=" + bitrate +
                        '}';
            }

            public String content_type;
            public String url;
            public long bitrate;

            public Variant(JSONParcel source) {
                content_type = source.readString("content_type");
                url = source.readString("url");
                bitrate = source.readLong("bitrate");
            }

            @Override
            public int describeContents() {
                return 0;
            }

            public static Variant[] fromMediaEntityVariants(MediaEntity.VideoInfo.Variant[] entityVariants) {
                if (entityVariants == null) return null;
                final Variant[] variants = new Variant[entityVariants.length];
                for (int i = 0, j = entityVariants.length; i < j; i++) {
                    variants[i] = new Variant(entityVariants[i]);
                }
                return variants;
            }


            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.content_type);
                dest.writeString(this.url);
                dest.writeLong(this.bitrate);
            }


            private Variant(Parcel in) {
                this.content_type = in.readString();
                this.url = in.readString();
                this.bitrate = in.readLong();
            }

            public static final Parcelable.Creator<Variant> CREATOR = new Parcelable.Creator<Variant>() {
                @Override
                public Variant createFromParcel(Parcel source) {
                    return new Variant(source);
                }

                @Override
                public Variant[] newArray(int size) {
                    return new Variant[size];
                }
            };

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedArray(variants, flags);
            dest.writeLong(duration);
        }


        private VideoInfo(Parcel in) {
            variants = in.createTypedArray(Variant.CREATOR);
            duration = in.readLong();
        }

        public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>() {
            @Override
            public VideoInfo createFromParcel(Parcel source) {
                return new VideoInfo(source);
            }

            @Override
            public VideoInfo[] newArray(int size) {
                return new VideoInfo[size];
            }
        };
    }

    public static ParcelableMedia newImage(final String media_url, final String url) {
        return new ParcelableMedia(media_url, url, media_url, 0, 0, TYPE_IMAGE);
    }

    public static class MediaSize {

        public static final int LARGE = 1;
        public static final int MEDIUM = 2;
        public static final int SMALL = 3;
        public static final int THUMB = 4;


    }

}

package org.techteam.decider.content.question;

import org.techteam.decider.content.entities.CategoryEntry;

public class ImageQuestionData extends QuestionData {
    public static class Picture {
        private String pictureId;
        private String text;

        public Picture(String pictureId, String text) {
            this.pictureId = pictureId;
            this.text = text;
        }

        public String getPictureId() {
            return pictureId;
        }

        public String getText() {
            return text;
        }
    }

    private Picture picture1;
    private Picture picture2;

    public ImageQuestionData(String text, CategoryEntry categoryEntry, boolean anonymous, Picture picture1, Picture picture2) {
        super(text, categoryEntry, anonymous);
        this.picture1 = picture1;
        this.picture2 = picture2;
    }

    public Picture getPicture1() {
        return picture1;
    }

    public Picture getPicture2() {
        return picture2;
    }

    @Override
    public String toJson() {
        return null;
    }

    @Override
    public String createFingerprint() {
        throw new RuntimeException("Unimplemented method");
//        return null;
    }
}

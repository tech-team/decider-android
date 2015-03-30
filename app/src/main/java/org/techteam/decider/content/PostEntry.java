package org.techteam.decider.content;


public abstract class PostEntry extends Entry {
    private boolean liked;

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}

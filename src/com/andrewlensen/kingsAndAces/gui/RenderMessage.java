package com.andrewlensen.kingsAndAces.gui;

/**
 * Created by Andrew on 16/03/14.
 */
public class RenderMessage {
    private final String messageText;
    private final boolean isError;

    @Override
    public String toString() {
        return "RenderMessage{" +
                "messageText='" + messageText + '\'' +
                ", isError=" + isError +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RenderMessage that = (RenderMessage) o;

        if (isError != that.isError) return false;
        if (messageText != null ? !messageText.equals(that.messageText) : that.messageText != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = messageText != null ? messageText.hashCode() : 0;
        result = 31 * result + (isError ? 1 : 0);
        return result;
    }

    public RenderMessage(String messageText, boolean isError){

        this.messageText = messageText;
        this.isError = isError;
    }

    public String getMessageText() {
        return messageText;
    }

    public boolean isError() {
        return isError;
    }
}

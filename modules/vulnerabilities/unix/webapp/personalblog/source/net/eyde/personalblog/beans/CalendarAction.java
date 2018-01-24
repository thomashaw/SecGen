package net.eyde.personalblog.beans;


/**
 * @author NEyde
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CalendarAction {
    int day;
    String url;

    /**
     * Returns the day.
     * @return int
     */
    public int getDay() {
        return day;
    }

    /**
     * Returns the url.
     * @return String
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the day.
     * @param day The day to set
     */
    public void setDay(int day) {
        this.day = day;
    }

    /**
     * Sets the url.
     * @param url The url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public boolean equals(Object obj) {
        return (obj instanceof CalendarAction) &&
        (this.day == ((CalendarAction) obj).day) &&
        (this.url.equals(((CalendarAction) obj).url));
    }
}

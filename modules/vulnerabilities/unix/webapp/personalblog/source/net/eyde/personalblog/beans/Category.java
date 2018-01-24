package net.eyde.personalblog.beans;


/**
 * @author NEyde
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Category {
    int id;
    String name;
    String description;
    char value;
    String image;

    
     /**
     * Sets the ID.
     * @param _id The ID to set
     */
    public void setId(int _id) {
        this.id = _id; 
    }
 
       /**
     * Returns the id.
     * @return int
     */
 
     public int getId() {
        return this.id; 
    }
    
    /**
     * Returns the image.
     * @return String
     */
    public String getImage() {
        return image;
    }

    /**
     * Returns the name.
     * @return String
     */
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Returns the value.
     * @return char
     */
    public char getValue() {
        return value;
    }

    /**
     * Sets the image.
     * @param image The image to set
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue(char value) {
        this.value = value;
    }
}

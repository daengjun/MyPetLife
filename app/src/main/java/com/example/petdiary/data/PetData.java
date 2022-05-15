package com.example.petdiary.data;

public class PetData {
    private String name;
    private String imageUrl;
    private String memo;
    private String petId;
    private String petMaster;

    public String getPetMaster() {
        return petMaster;
    }

    public String getPetId() {
        return petId;
    }

    public PetData(String petId, String name, String imageUrl, String memo, String master) {
        this.petId = petId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.memo = memo;
        this.petMaster = master;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getMemo() {
        return memo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}

package com.kitsune.foursquarehelper;

import java.util.ArrayList;
import java.util.List;

public class Venue
{
	
	String id;
	String storeId;
	String referralId;
	String name;
	
	String contactPhone;
	String contactFormattedPhone;
	
	String canonicalUrl;
	
	String locationAddress;
	String locationCrossStreet;
	String locationLatitude;
	String locationLongitude;
	String locationDistance;
	String locationPostalCode;
	String locationCity;
	String locationState;
	String locationCountry;
	String locationCC;
	
	boolean verified;
	
	int statsCheckinsCount;
	int statsUsersCount;
	int statsTipCount;
	
	String menuType;
	String menuLabel;
	String menuAnchor;
	String menuURL;
	String menuMobileURL;
	
	int hereNowCount;
	
	int photosCount;
	List<PhotosItem> photosItems;
	
	public Venue()
	{
		photosItems = new ArrayList<PhotosItem>();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getReferralId() {
		return referralId;
	}

	public void setReferralId(String referralId) {
		this.referralId = referralId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getContactFormattedPhone() {
		return contactFormattedPhone;
	}

	public void setContactFormattedPhone(String contactFormattedPhone) {
		this.contactFormattedPhone = contactFormattedPhone;
	}

	public String getCanonicalUrl() {
		return canonicalUrl;
	}

	public void setCanonicalUrl(String canonicalUrl) {
		this.canonicalUrl = canonicalUrl;
	}

	public String getLocationAddress() {
		return locationAddress;
	}

	public void setLocationAddress(String locationAddress) {
		this.locationAddress = locationAddress;
	}

	public String getLocationCrossStreet() {
		return locationCrossStreet;
	}

	public void setLocationCrossStreet(String locationCrossStreet) {
		this.locationCrossStreet = locationCrossStreet;
	}

	public String getLocationLatitude() {
		return locationLatitude;
	}

	public void setLocationLatitude(String locationLatitude) {
		this.locationLatitude = locationLatitude;
	}

	public String getLocationLongitude() {
		return locationLongitude;
	}

	public void setLocationLongitude(String locationLongitude) {
		this.locationLongitude = locationLongitude;
	}

	public String getLocationDistance() {
		return locationDistance;
	}

	public void setLocationDistance(String locationDistance) {
		this.locationDistance = locationDistance;
	}

	public String getLocationPostalCode() {
		return locationPostalCode;
	}

	public void setLocationPostalCode(String locationPostalCode) {
		this.locationPostalCode = locationPostalCode;
	}

	public String getLocationCity() {
		return locationCity;
	}

	public void setLocationCity(String locationCity) {
		this.locationCity = locationCity;
	}

	public String getLocationState() {
		return locationState;
	}

	public void setLocationState(String locationState) {
		this.locationState = locationState;
	}

	public String getLocationCountry() {
		return locationCountry;
	}

	public void setLocationCountry(String locationCountry) {
		this.locationCountry = locationCountry;
	}

	public String getLocationCC() {
		return locationCC;
	}

	public void setLocationCC(String locationCC) {
		this.locationCC = locationCC;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public int getStatsCheckinsCount() {
		return statsCheckinsCount;
	}

	public void setStatsCheckinsCount(int statsCheckinsCount) {
		this.statsCheckinsCount = statsCheckinsCount;
	}

	public int getStatsUsersCount() {
		return statsUsersCount;
	}

	public void setStatsUsersCount(int statsUsersCount) {
		this.statsUsersCount = statsUsersCount;
	}

	public int getStatsTipCount() {
		return statsTipCount;
	}

	public void setStatsTipCount(int statsTipCount) {
		this.statsTipCount = statsTipCount;
	}

	public String getMenuType() {
		return menuType;
	}

	public void setMenuType(String menuType) {
		this.menuType = menuType;
	}

	public String getMenuLabel() {
		return menuLabel;
	}

	public void setMenuLabel(String menuLabel) {
		this.menuLabel = menuLabel;
	}

	public String getMenuAnchor() {
		return menuAnchor;
	}

	public void setMenuAnchor(String menuAnchor) {
		this.menuAnchor = menuAnchor;
	}

	public String getMenuURL() {
		return menuURL;
	}

	public void setMenuURL(String menuURL) {
		this.menuURL = menuURL;
	}

	public String getMenuMobileURL() {
		return menuMobileURL;
	}

	public void setMenuMobileURL(String menuMobileURL) {
		this.menuMobileURL = menuMobileURL;
	}

	public int getHereNowCount() {
		return hereNowCount;
	}

	public void setHereNowCount(int hereNowCount) {
		this.hereNowCount = hereNowCount;
	}
	
	public int getPhotosCount() {
		return photosCount;
	}

	public void setPhotosCount(int photosCount) {
		this.photosCount = photosCount;
	}

	public List<PhotosItem> getPhotosItems() {
		return photosItems;
	}

	public void addPhotosItems( PhotosItem photosItems) {
		this.photosItems.add(photosItems);
	}
	
	/**
	 * get photo url, if width is not specified it will get the image default width
	 * @param position photos index
	 * @return image URL string
	 */
	public String getPhotoURL( int position )
	{
		String imageURl = null;
		if( this.getPhotosCount() > 0 )
		{
			PhotosItem photo 	= photosItems.get(position);
			int imageWidth	= photo.getWidth();
			imageURl 			= photo.getPrefix() + "width" + imageWidth + photo.getSuffix();
		}
		return imageURl;
	}
	
	/**
	 * get photo url, if width is not specified it will get the image default width
	 * @param position photos index
	 * @param imageWidth image width in pixel
	 * @return image URL string
	 */
	public String getPhotoURL( int position, int imageWidth )
	{
		String imageURl = null;
		if( this.getPhotosCount() > 0 )
		{
			PhotosItem photo 	= photosItems.get(position);
			imageURl 			= photo.getPrefix() + "width" + imageWidth + photo.getSuffix();
		}
		return imageURl;
	}
	
}


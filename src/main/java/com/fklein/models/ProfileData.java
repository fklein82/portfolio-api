package com.fklein.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ProfileData {

    @JsonProperty("personalInfo")
    private PersonalInfo personalInfo;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("experience")
    private List<Experience> experience;

    @JsonProperty("education")
    private List<Education> education;

    @JsonProperty("certifications")
    private List<Certification> certifications;

    @JsonProperty("skills")
    private Skills skills;

    @JsonProperty("languages")
    private List<Language> languages;

    @JsonProperty("projects")
    private List<Project> projects;

    @JsonProperty("interests")
    private List<String> interests;

    @JsonProperty("metadata")
    private Map<String, String> metadata;

    // Getters and Setters
    public PersonalInfo getPersonalInfo() {
        return personalInfo;
    }

    public void setPersonalInfo(PersonalInfo personalInfo) {
        this.personalInfo = personalInfo;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Experience> getExperience() {
        return experience;
    }

    public void setExperience(List<Experience> experience) {
        this.experience = experience;
    }

    public List<Education> getEducation() {
        return education;
    }

    public void setEducation(List<Education> education) {
        this.education = education;
    }

    public List<Certification> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<Certification> certifications) {
        this.certifications = certifications;
    }

    public Skills getSkills() {
        return skills;
    }

    public void setSkills(Skills skills) {
        this.skills = skills;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    // Nested classes
    public static class PersonalInfo {
        private String name;
        private String title;
        private String company;
        private String location;
        private String email;
        private String phone;
        private String linkedIn;
        private String github;
        private String twitter;
        private String website;
        private String blog;
        private String portfolio;
        private String followers;
        private String connections;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getLinkedIn() { return linkedIn; }
        public void setLinkedIn(String linkedIn) { this.linkedIn = linkedIn; }
        public String getGithub() { return github; }
        public void setGithub(String github) { this.github = github; }
        public String getTwitter() { return twitter; }
        public void setTwitter(String twitter) { this.twitter = twitter; }
        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
        public String getBlog() { return blog; }
        public void setBlog(String blog) { this.blog = blog; }
        public String getPortfolio() { return portfolio; }
        public void setPortfolio(String portfolio) { this.portfolio = portfolio; }
        public String getFollowers() { return followers; }
        public void setFollowers(String followers) { this.followers = followers; }
        public String getConnections() { return connections; }
        public void setConnections(String connections) { this.connections = connections; }
    }

    public static class Experience {
        private String title;
        private String company;
        private String location;
        private boolean current;
        private String startDate;
        private String endDate;
        private String description;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public boolean isCurrent() { return current; }
        public void setCurrent(boolean current) { this.current = current; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class Education {
        private String institution;
        private String degree;
        private String field;
        private String location;
        private int startYear;
        private int endYear;
        private String description;

        // Getters and Setters
        public String getInstitution() { return institution; }
        public void setInstitution(String institution) { this.institution = institution; }
        public String getDegree() { return degree; }
        public void setDegree(String degree) { this.degree = degree; }
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public int getStartYear() { return startYear; }
        public void setStartYear(int startYear) { this.startYear = startYear; }
        public int getEndYear() { return endYear; }
        public void setEndYear(int endYear) { this.endYear = endYear; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class Certification {
        private String name;
        private String issuer;
        private String date;
        private String credentialId;
        private String description;
        private boolean featured;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getCredentialId() { return credentialId; }
        public void setCredentialId(String credentialId) { this.credentialId = credentialId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isFeatured() { return featured; }
        public void setFeatured(boolean featured) { this.featured = featured; }
    }

    public static class Skills {
        private List<String> technical;
        private List<String> soft;

        // Getters and Setters
        public List<String> getTechnical() { return technical; }
        public void setTechnical(List<String> technical) { this.technical = technical; }
        public List<String> getSoft() { return soft; }
        public void setSoft(List<String> soft) { this.soft = soft; }
    }

    public static class Language {
        private String language;
        private String proficiency;

        // Getters and Setters
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getProficiency() { return proficiency; }
        public void setProficiency(String proficiency) { this.proficiency = proficiency; }
    }

    public static class Project {
        private String name;
        private String url;
        private String description;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

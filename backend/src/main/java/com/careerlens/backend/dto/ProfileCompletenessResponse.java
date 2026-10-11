package com.careerlens.backend.dto;

import java.util.List;

public class ProfileCompletenessResponse {
    private int percentage;
    private List<SectionStatus> sections;
    private List<String> missingFields;
    private String tip;

    public ProfileCompletenessResponse() {}

    public ProfileCompletenessResponse(int percentage, List<SectionStatus> sections, List<String> missingFields, String tip) {
        this.percentage = percentage;
        this.sections = sections;
        this.missingFields = missingFields;
        this.tip = tip;
    }

    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }

    public List<SectionStatus> getSections() { return sections; }
    public void setSections(List<SectionStatus> sections) { this.sections = sections; }

    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }

    public String getTip() { return tip; }
    public void setTip(String tip) { this.tip = tip; }

    public static class SectionStatus {
        private String name;
        private int weight;
        private boolean complete;
        private String description;

        public SectionStatus() {}

        public SectionStatus(String name, int weight, boolean complete, String description) {
            this.name = name;
            this.weight = weight;
            this.complete = complete;
            this.description = description;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getWeight() { return weight; }
        public void setWeight(int weight) { this.weight = weight; }

        public boolean isComplete() { return complete; }
        public void setComplete(boolean complete) { this.complete = complete; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

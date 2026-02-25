package com.plavonra.people.mapper;

import com.plavonra.services.people.api.model.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.ai.document.Document;

@Mapper(componentModel = "spring")
public interface ActionSuggestionMapper {

  default ActionSuggestionResponse toResponse(
      String text, String actionCode, double confidenceScore, List<Document> documents) {

    ActionSuggestionResponse response = new ActionSuggestionResponse();
    response.setInput(text);
    response.setAction(toActionInfo(actionCode));
    response.setConfidence(toConfidenceInfo(confidenceScore));
    response.setBasedOn(toBasedOn(documents));
    return response;
  }

  default ActionInfo toActionInfo(String code) {
    if (code == null) code = "REVIEW";

    ActionInfo action = new ActionInfo();
    action.setCode(code);
    action.setLabel(actionLabel(code));
    action.setDescription(actionDescription(code));
    return action;
  }

  default String actionLabel(String code) {
    return switch (code) {
      case "UPDATED" -> "Update customer record";
      case "ESCALATED" -> "Escalate to supervisor";
      case "REFUNDED" -> "Issue refund";
      case "CONTACT_CUSTOMER" -> "Contact the customer";
      default -> "Manual review required";
    };
  }

  default ConfidenceInfo toConfidenceInfo(double score) {
    ConfidenceInfo confidence = new ConfidenceInfo();

    String level = score > 0.75 ? "HIGH" : score > 0.55 ? "MEDIUM" : "LOW";

    confidence.setLevel(ConfidenceInfo.LevelEnum.fromValue(level));
    confidence.setScore(score);
    confidence.setLabel(confidenceLabel(level));

    return confidence;
  }

  default BasedOn toBasedOn(List<Document> documents) {
    BasedOn basedOn = new BasedOn();
    List<SimilarCase> objects = documents.stream().map(this::toSimilarCase).toList();

    basedOn.setCount(documents.size());
    basedOn.setSimilarCases(objects);
    return basedOn;
  }

  @Mapping(target = "personId", source = "metadata", qualifiedByName = "personId")
  @Mapping(target = "note", source = "text")
  @Mapping(target = "similarity", source = "score", qualifiedByName = "roundSimilarity")
  @Mapping(target = "actionTaken", source = "metadata", qualifiedByName = "action")
  SimilarCase toSimilarCase(Document document);

  @Named("personId")
  default UUID mapPersonId(Map<String, Object> metadata) {
    Object value = metadata.get("personId");
    return value != null ? UUID.fromString(value.toString()) : null;
  }

  @Named("action")
  default String mapAction(Map<String, Object> metadata) {
    Object value = metadata.get("action");
    return value != null ? value.toString() : null;
  }

  @Named("roundSimilarity")
  default double roundSimilarity(double score) {
    return Math.round(score * 1000.0) / 1000.0;
  }

  @Named("actionDescription")
  default String actionDescription(String code) {
    return switch (code) {
      case "UPDATED" -> "Modify billing or profile information";
      case "ESCALATED" -> "Requires higher level support";
      case "REFUNDED" -> "Refund payment to customer";
      case "CONTACT_CUSTOMER" -> "Request additional details";
      default -> "AI could not determine a confident action";
    };
  }

  @Named("confidenceLabel")
  default String confidenceLabel(String level) {
    return switch (level) {
      case "HIGH" -> "High confidence";
      case "MEDIUM" -> "Moderate confidence";
      default -> "Low confidence";
    };
  }
}

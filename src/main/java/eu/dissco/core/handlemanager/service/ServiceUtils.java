package eu.dissco.core.handlemanager.service;

import eu.dissco.core.handlemanager.domain.requests.attributes.DigitalSpecimenRequest;
import eu.dissco.core.handlemanager.domain.requests.attributes.MediaObjectRequest;
import eu.dissco.core.handlemanager.domain.requests.attributes.PhysicalIdType;
import java.nio.charset.StandardCharsets;

public class ServiceUtils {

  private ServiceUtils(){}

  public static <T extends DigitalSpecimenRequest> byte[] setUniquePhysicalIdentifierId(T request) {
    var physicalIdentifier = request.getPrimarySpecimenObjectId();
    if (request.getPrimarySpecimenObjectIdType().equals(PhysicalIdType.CETAF)) {
      return physicalIdentifier.getBytes(StandardCharsets.UTF_8);
    }
    return concatIds(physicalIdentifier, request.getSpecimenHost());
  }

  public static byte[] setUniquePhysicalIdentifierId(MediaObjectRequest request) {
    var physicalIdentifier = request.getSubjectPhysicalIdentifier();
    if (physicalIdentifier.physicalIdType().equals(PhysicalIdType.CETAF)) {
      return physicalIdentifier.physicalId().getBytes(StandardCharsets.UTF_8);
    }
    return concatIds(physicalIdentifier.physicalId(), request.getSubjectSpecimenHostPid());
  }

  private static byte[] concatIds(String physicalIdentifier, String specimenHostPid) {
    var hostIdArr = specimenHostPid.split("/");
    var hostId = hostIdArr[hostIdArr.length - 1];
    return (physicalIdentifier + ":" + hostId).getBytes(StandardCharsets.UTF_8);
  }

}
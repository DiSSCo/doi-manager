package eu.dissco.core.handlemanager.exceptions;

public class PidServiceInternalError extends Exception {

  // Response code = 422 UNPROCESSABLE ENTITY
  // Possible causes: IOException, ParserConfigurationException, TransformerException

  public PidServiceInternalError(String s, Throwable exceptionCause) {
    super(s, exceptionCause);
  }

}

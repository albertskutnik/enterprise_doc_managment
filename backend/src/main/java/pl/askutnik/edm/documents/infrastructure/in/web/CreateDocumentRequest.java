package pl.askutnik.edm.documents.infrastructure.in.web;

public class CreateDocumentRequest {

    private String name;
    private String contentType;
    private long size;

    public CreateDocumentRequest() {
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }
}

package pl.askutnik.edm.documents.usecase.port.in;

public class CreateDocumentCommand {
    
    private String name;
    private String contentType;
    private long size;

    public CreateDocumentCommand(String name, String contentType, long size) {
        this.name = name;
        this.contentType = contentType;
        this.size = size;
    }

    public String getName(){
        return name;
    }

    public String getContentType(){
        return contentType;
    }

    public long getSize() {
        return size;
    }


}

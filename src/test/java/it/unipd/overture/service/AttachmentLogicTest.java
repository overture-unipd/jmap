package it.unipd.overture.service;

import rs.ltt.jmap.common.entity.Upload;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unipd.overture.port.out.AttachmentPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class AttachmentLogicTest {
    @Mock AttachmentPort attachmentPort;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void uploadTest() {
        String blobId = "mockBlobId";
        byte[] data = {3, -2};
        AttachmentLogic attachmentLogic = new AttachmentLogic(attachmentPort);
        when(attachmentPort.insert(data, "application/octet-stream", Long.valueOf(2))).thenReturn(blobId);
        Upload upload = attachmentLogic.upload(data, "application/octet-stream", Long.valueOf(2));
        Assertions.assertEquals(blobId, upload.getBlobId());
    }

    @Test
    public void downloadTest() {
        String blobId = "mockBlobId";
        byte[] data = {3, -2};
        AttachmentLogic attachmentLogic = new AttachmentLogic(attachmentPort);
        when(attachmentPort.get(blobId)).thenReturn(data);
        Assertions.assertEquals(data, attachmentLogic.download(blobId));
    }
}

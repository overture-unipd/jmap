package it.unipd.overture.controller;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.gson.Gson;

import it.unipd.overture.service.AttachmentLogic;
import rs.ltt.jmap.common.entity.Upload;

public class AttachmentControllerTest {
    @Mock AttachmentLogic attachmentLogic;
    @Mock Gson gson;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void pullTest() {
        String id = "Id";
        byte[] data = {3, -2};
        AttachmentController attachmentController = new AttachmentController(gson, attachmentLogic);
        when(attachmentLogic.download(id)).thenReturn(data);
        Assertions.assertEquals(data, attachmentController.pull(id));
    }

    @Test
    public void pushTest() {
        String id = "Id";
        byte[] data = {3, -2};
        AttachmentController attachmentController = new AttachmentController(gson, attachmentLogic);
        when(attachmentLogic.upload(data, "application/octet-stream", Long.valueOf(2))).thenReturn(Upload.builder()
                                                            .blobId(id)
                                                            .build());
        when(gson.toJson(Mockito.any(Upload.class))).thenReturn("{\"accountId\":\"ziopera\", \"blobId\": \""+ id + "\", \"type\": \"byte[]\", \"size\": \"2[]\"}");
        String s = attachmentController.push(data, "application/octet-stream", Long.valueOf(2));
        Assertions.assertEquals("{\"accountId\":\"ziopera\", \"blobId\": \""+ id + "\", \"type\": \"byte[]\", \"size\": \"2[]\"}", s);
    }
}

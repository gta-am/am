package controllers;

import play.*;
import play.mvc.*;

import java.io.File;
import java.util.*;

import models.*;
import utils.FileUtil;

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void upLoadXls() {
        render();
    }

    public static void uploadFile(File file) {
        FileUtil.uploadFile(file);
    }
}
package com.csed.paint.ui.controller;

import com.csed.paint.ui.dataCenter.Redo;
import com.csed.paint.ui.dataCenter.Undo;
import com.csed.paint.ui.dataCenter.data;
import com.csed.paint.ui.model.Factory;
import com.csed.paint.ui.model.Shape;
import com.csed.paint.ui.model.Square;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.catalina.User;
import org.apache.commons.io.FilenameUtils;
import org.apache.coyote.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import com.fasterxml.jackson.core.JsonParser;
import com.csed.paint.ui.dataCenter.Redo;
import com.csed.paint.ui.dataCenter.Undo;
import com.csed.paint.ui.dataCenter.data;
import com.csed.paint.ui.model.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;

@CrossOrigin("http://localhost:8081")
@RestController
@RequestMapping()

public class Controller {

    @PostMapping(path = "/{type}")
    public ResponseEntity<Void> createObject(@RequestBody Shape shape, @PathVariable String type) {
        try {
            Shape newShape = Factory.createShape(type, shape);
            data.shapes.put(shape.getId(), newShape);
            Undo.opStack.add(0); // op-code of POST request =0 ;
            Undo.objStack.add(newShape);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    @PostMapping(path = "/all/json")
    public ResponseEntity<Void> saveFileJSON(@RequestParam(value = "path") String path) {
        try {
            File file = new File(path + ".json");
            if (file.exists()) {
                String directory = file.getParent();
                String fileName = file.getName();
                String baseName = FilenameUtils.getBaseName(fileName);
                String extension = FilenameUtils.getExtension(fileName);
                int counter = 1;
                String orig_baseName = baseName;
                do {
                    baseName = orig_baseName + '(' + counter + ')';
                    fileName = baseName + "." + extension;
                    file = new File(directory, fileName);
                    counter++;
                }
                while (file.exists());

            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(file, data.shapes.values());
            data.shapes.clear();
            Undo.objStack.clear();
            Undo.opStack.clear();
            Redo.objStack.clear();
            Redo.opStack.clear();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }

    }

    @PostMapping(path = "/all/xml")
    public ResponseEntity<Void> saveFileXML(@RequestParam(value = "path") String path) {
        try {
            File file = new File(path + ".xml");
            if (file.exists()) {
                String directory = file.getParent();
                String fileName = file.getName();
                String baseName = FilenameUtils.getBaseName(fileName);
                String extension = FilenameUtils.getExtension(fileName);
                int counter = 1;
                String orig_baseName = baseName;
                do {
                    baseName = orig_baseName + '(' + counter + ')';
                    fileName = baseName + "." + extension;
                    file = new File(directory, fileName);
                    counter++;
                }
                while (file.exists());
            }
            FileOutputStream fos = new FileOutputStream(file);
            XMLEncoder encoder = new XMLEncoder(fos);
            encoder.writeObject(data.shapes);
            encoder.close();
            data.shapes.clear();
            Undo.objStack.clear();
            Undo.opStack.clear();
            Redo.objStack.clear();
            Redo.opStack.clear();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }
    @PutMapping("/{type}")
    public ResponseEntity<Void> editObject(@RequestBody Shape shape, @PathVariable String type) {
        if (!data.shapes.containsKey(shape.getId())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Shape oldShape = data.shapes.get(shape.getId());
        Shape newShape = Factory.createShape(type, shape);
        data.shapes.put(shape.getId(), newShape);
        Undo.opStack.add(1); // op-code of PUT request =1 ;
        Undo.objStack.add(oldShape);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteObject(@PathVariable long id) {
        if (!data.shapes.containsKey(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Shape shape = data.shapes.get(id);
        data.shapes.remove(id);
        Undo.opStack.add(2); // op-code of delete request =2;
        Undo.objStack.add(shape);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @DeleteMapping(path = "/all")
    public ResponseEntity<Void> clearALl() {
        try {
            data.shapes.clear() ;
            Undo.opStack.clear();
            Undo.objStack.clear();
            Redo.opStack.clear();
            Redo.objStack.clear();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }

    }

    @GetMapping(path = "/all/json")
    public ResponseEntity<Collection<Shape>> loadFileJSON(@RequestParam(value = "path") String path) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JSONParser parser = new JSONParser();
            FileReader file = new FileReader(path+ ".json");
            Object objectArr = parser.parse(file);
            JSONArray jsonArr = (JSONArray) objectArr;
            data.shapes.clear();
            Undo.objStack.clear();
            Undo.opStack.clear();
            Redo.objStack.clear();
            Undo.opStack.clear();
            for (Object object : jsonArr) {
                JSONObject jsonObject = (JSONObject) object;
                String jsonString = jsonObject.toString();
                Shape shape = mapper.readValue(jsonString, Shape.class);
                data.shapes.put(shape.getId(), shape);
            }
            return new ResponseEntity<>(data.shapes.values(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    @GetMapping(path = ("/all/xml"))
    public ResponseEntity<Collection<Shape>> getALLXML(@RequestParam(value = "path") String path){
        try {
            File file = new File(path+".xml");
            FileInputStream fis = new FileInputStream(file);
            XMLDecoder xmlDecoder = new XMLDecoder(fis);
            data.shapes.clear();
            Undo.objStack.clear();
            Undo.opStack.clear();
            Redo.objStack.clear();
            Undo.opStack.clear();
            data.shapes = (HashMap<Long, Shape>) xmlDecoder.readObject();
            return new ResponseEntity<>(data.shapes.values(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }


    @GetMapping(path = "/{id}")
    public ResponseEntity<Shape> getObject(@PathVariable long id) {
        if (!data.shapes.containsKey(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(data.shapes.get(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<HashMap<Long, Shape>> getAll() {
        try {
            return new ResponseEntity<>(data.shapes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    @GetMapping(path = {"/undo"})

    public ResponseEntity<Shape> undo() {

        if (Undo.objStack.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
        int operation = Undo.opStack.pop();
        Shape shape = Undo.objStack.pop();
        switch (operation) {
            case 0:
                data.shapes.remove(shape.getId());
                Redo.opStack.add(operation);
                Redo.objStack.add(shape);
                return new ResponseEntity<>(shape, HttpStatus.OK);
            case 1:
                Shape newShape = data.shapes.get(shape.getId());
                data.shapes.put(shape.getId(), shape);
                Redo.opStack.add(operation);
                Redo.objStack.add(newShape);
                return new ResponseEntity<>(shape, HttpStatus.OK);

            case 2:
                data.shapes.put(shape.getId(), shape);
                Redo.opStack.add(operation);
                Redo.objStack.add(shape);
                return new ResponseEntity<>(shape, HttpStatus.OK);
            default:
                return null;
        }
    }

    @GetMapping(path = {"/redo"})
    public ResponseEntity<Shape> redo() {
        if (Redo.objStack.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
        Shape shape = Redo.objStack.pop();
        int operation = Redo.opStack.pop();
        switch (operation) {
            case 0:
                data.shapes.put(shape.getId(), shape);
                Undo.opStack.add(operation);
                Undo.objStack.add(shape);
                return new ResponseEntity<>(shape, HttpStatus.OK);
            case 1:
                Shape oldShape = data.shapes.get(shape.getId());
                Undo.opStack.add(operation);
                Undo.objStack.add(oldShape);
                data.shapes.put(shape.getId(), shape);
                return new ResponseEntity<>(shape, HttpStatus.OK);
            case 2:
                data.shapes.remove(shape.getId());
                Undo.opStack.add(operation);
                Undo.objStack.add(shape);
                return new ResponseEntity<>(shape, HttpStatus.OK);
            default:
                return null;
        }

    }
}


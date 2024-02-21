<template>
  <div  id="saveLoad">
    <button v-for="button in upper_buttons_load" :key="button.value" @click="handleButtonClick(button)" >
        <img :src="button.imagePath" alt="shape-icon" width="15" height="15">
           {{ button.label }}  
        </button>
      
      <input type="text" v-model="filePath" placeholder="Enter file path to load">
      <input type="text" v-model="fileName" placeholder="Enter file path to save">
      <button v-for="button in upper_buttons_save" :key="button.value" @click="handleButtonClick(button)" >
        <img :src="button.imagePath" alt="shape-icon" width="15" height="15">
           {{ button.label }}  
        </button>
  </div>
  <div class="hello">
    <div class="ToolBox">
      <button v-for="button in left_buttons" :key="button.value" @click="handleButtonClick(button)" >
        <img :src="button.imagePath" alt="shape-icon" width="40" height="40">
           <!-- {{ button.label }}  -->
        </button>
    </div>
    <div class="konvaHolder">
      <v-stage
      ref="stage"
      :config="configKonva"
      style="width: 100%; height: 100%;"
      >
      <v-layer :config="{ listening: true }" ref="layer">
      </v-layer>
    </v-stage>
    </div>
    <div :class="{ 'show-message': showMessage }" class="message">
      {{ messageText }}
      </div>
    <div class="ToolBox">
      <input type="color" v-model="selectedColor" />
      <button v-for="button in right_buttons" 
      :key="button.value" @click="handleButtonClick(button)" 
      :class="{ 'button-clicked': button.value ==='color' &&ButtonClicked }">
      <img :src="button.imagePath" alt="shape-icon" width="25" height="25">
      </button>
    </div>
  </div>
</template>

<script>
const width = window.innerWidth;
const height = window.innerHeight;
import Konva from "konva";
import axios from 'axios'

export default {
  name: "pageLayout",

  data() {
    return {
      configKonva: {
        width: width,
        height: height,
      },

      left_buttons: [
      { label: "Line", value: "line", imagePath: require("@/assets/line.png")},
        { label: "Circle", value: "circle", imagePath: require("@/assets/circle.png") },
        { label: "Ellipse", value: "ellipse" , imagePath: require("@/assets/ellipse.png")},
        { label: "Square", value: "square" , imagePath: require("@/assets/square.png")},
        { label: "Rectangle", value: "rectangle", imagePath: require("@/assets/rectangle.png") },
        { label: "Triangle", value: "triangle", imagePath: require("@/assets/triangle.png") },
      ],

      right_buttons: [
       { label: "Color", value: "color" , imagePath: require("@/assets/color.png")}, 
        { label: "Copy", value: "copy" , imagePath: require("@/assets/copy.png")}, 
        { label: "Delete", value: "delete", imagePath: require("@/assets/delete.png")}, 
        { label: "Undo", value: "undo" , imagePath: require("@/assets/undo.png")}, 
        { label: "Redo", value: "redo" , imagePath: require("@/assets/redo.png")}, 
      ],
      upper_buttons_save:[
        { label: " JSON", value: "saveJSON", imagePath: require("@/assets/save.png")}, 
        { label: " XML", value: "saveXML", imagePath: require("@/assets/save.png")}, 
        ],
       upper_buttons_load:[
         { label: " JSON", value: "loadJSON", imagePath: require("@/assets/load.png")}, 
        { label: " XML", value: "loadXML", imagePath: require("@/assets/load.png")}, 
        ],

      shapes:[],

      selectedShape: null,
      selectedColor: "#000000",
      fill: null,
      id: 1,
      isDrawing: false,
      coloring: false,
      ButtonClicked: false,
      showMessage: false,
      messageText: "",
      line: false,
      circle: false,
      ellipse: false,
      square: false,
      rectangle: false,
      triangle: false,
      selectedLeftButton: null,

     

      filePath: "",
      fileName: "",
      otherVariable: "",

      dummyBackObj: {
      id: null,
      color: null,
      type: null,
      properties: []
    },

    };
  },

  mounted() {
    if(this.shapes.length===0){
      axios.delete(`http://localhost:8080/all`)
         .then(response => {
        console.log('Shape deleted successfully:', response.data);
      })
      .catch(error => {
        console.error('Error deleting shape:', error);
      });
    }
    
    this.stage = new Konva.Stage({
      container: this.$refs.stage.$el,
      width: this.configKonva.width,
      height: this.configKonva.height,
    });

    this.layer = new Konva.Layer();

    this.handleResize();
    window.addEventListener("resize", this.handleResize);
  },
  methods: {
    handleResize() {
      this.stage.width(window.innerWidth);
      this.stage.height(window.innerHeight);
    },

    handleButtonClick(button) {
      const { value } = button;
      console.log("Button " + value + " clicked!");
      this.selectedLeftButton = value;
      //this.draw(value);
      this.stage.add(this.layer);
      if (value === "rectangle") {
        this.line = false;
        this.circle = false;
        this.ellipse = false;
        this.square = false;
        this.rectangle = true;
        this.triangle = false;
        //this.draw("rectangle");
        this.drawRectangle();
        this.stage.add(this.layer);
      } else if (value === "circle") {
        this.line = false;
        this.circle = true;
        this.ellipse = false;
        this.square = false;
        this.rectangle = false;
        this.triangle = false;
        //this.draw("circle");
        this.drawCircle();
        this.stage.add(this.layer);
      } else if (value === "ellipse") {
        this.line = false;
        this.circle = false;
        this.ellipse = true;
        this.square = false;
        this.rectangle = false;
        this.triangle = false;
        //this.draw("ellipse");
        this.drawEllipse();
        this.stage.add(this.layer);
      } else if (value === "line") {
        this.line = true;
        this.circle = false;
        this.ellipse = false;
        this.square = false;
        this.rectangle = false;
        this.triangle = false;
        this.drawLineSegment();
        this.stage.add(this.layer);
      } else if (value === "square") {
        this.line = false;
        this.circle = false;
        this.ellipse = false;
        this.square = true;
        this.rectangle = false;
        this.triangle = false;
        this.drawSquare();
        this.stage.add(this.layer);
      } else if (value === "triangle") {
        this.line = false;
        this.circle = false;
        this.ellipse = false;
        this.square = false;
        this.rectangle = false;
        this.triangle = true;
        this.drawTriangle();
        this.stage.add(this.layer);
      } else if (value === "color") {
        this.coloring = !this.coloring;
        this.ButtonClicked = !this.ButtonClicked;
        this.stage.add(this.layer);
      } else if (value === "copy") {
        this.copySelectedShape();
        this.stage.add(this.layer);
      } else if(value ==="delete"){
        this.deleteSelectedShape();
        this.stage.add(this.layer);
      }else if(value ==="info"){
        console.log(this.shapes);
      } else if(value === "saveJSON"){
        if(this.fileName === ""){
          this.showMessage = true;
          this.messageText = "No path is entered!";
          setTimeout(() => {
            this.showMessage = false;
          }, 3000);

        }else{

        this.otherVariable = this.fileName.replace(/\\/g, '/');

        console.log(this.otherVariable);
        axios.post(`http://localhost:8080/all/json?path=${this.otherVariable}`)
        .then(response => {
          console.log('File saved successfully:', response.data);
          this.showMessage = true;
          this.messageText = "JSON File saved successfully!";
          setTimeout(() => {
            this.showMessage = false;
          }, 3000);
        })
        .catch(error => {
          console.error('Error saving File:', error);
        });}
      }
      else if(value === "saveXML"){
        if(this.fileName === ""){
          this.showMessage = true;
          this.messageText = "No path is entered!";
          setTimeout(() => {
            this.showMessage = false;
          }, 3000);

        }else{

        this.otherVariable = this.fileName.replace(/\\/g, '/');

        console.log(this.otherVariable);
        axios.post(`http://localhost:8080/all/xml?path=${this.otherVariable}`)
        .then(response => {
          console.log('File saved successfully:', response.data);
          this.showMessage = true;
          this.messageText = "XML File saved successfully!";
          setTimeout(() => {
            this.showMessage = false;
          }, 3000);
        })
        .catch(error => {
          console.error('Error saving File:', error);
        });}
      }  else if(value ==="loadJSON"){
        this.loadFileJSON();
      }else if(value === "loadXML"){
        this.loadFileXML();
      }
      else if(value === "undo"){
        this.undo();
      }else if(value === "redo"){
          this.redo();
      }
    },
    /*draw(value){
        let shape = null;
        this.stage.on('mousedown', () => {
          console.log(value);
          
          this.isDrawing = true;
          const pos = this.stage.getPointerPosition();

          if (value === "rectangle") {
            shape = new Konva.Rect({
              x: pos.x,
              y: pos.y,
              width: 0,
              height: 0,
              stroke: 'black',
              //strokeWidth: 2,
            });
          } else if (value === "circle") {
            shape = new Konva.Circle({
              x: pos.x,
              y: pos.y,
              radius: 0,
              stroke: 'black',
              //strokeWidth: 2,
            });
          } else if (value === "ellipse") {
            shape = new Konva.Ellipse({
              x: pos.x,
              y: pos.y,
              radiusX: 0,
              radiusY: 0,
              stroke: 'black',
              //strokeWidth: 2,
            });
          }// else if (value === "line") {
        
          // } else if (value === "square") {
        
          // } else { //(value === "triangle")
       
          // } 
          this.layer.add(shape);
          this.stage.add(this.layer);
        });
        this.stage.on('mousemove', () => {
          if (!this.isDrawing) {
            return;
          }
          const pos = this.stage.getPointerPosition();

          if (value === "rectangle") {
            const width = pos.x - shape.x();
            const height = pos.y - shape.y();
            shape.width(width);
            shape.height(height);
          } else if (value === "circle") {
            const radius = Math.sqrt(Math.pow(pos.x - shape.x(), 2) + Math.pow(pos.y - shape.y(), 2));
            shape.radius(radius);
          } else if (value === "ellipse") {
            const radiusX = Math.abs(pos.x - shape.x());
            const radiusY = Math.abs(pos.y - shape.y());
            shape.radiusX(radiusX);
            shape.radiusY(radiusY);
          }

          this.layer.batchDraw();
        });
        this.stage.on('mouseup', () => {
          this.isDrawing = false;
          console.log(shape);
          shape = null;
        })
    },*/
    loadFileJSON() {
      this.otherVariable = this.filePath.replace(/\\/g, '//');

        if (this.otherVariable) {
          //const url = ;

          axios.get(`http://localhost:8080/all/json?path=${this.otherVariable}`)
            .then(response => {
              this.shapes = []; 
              this.layer.destroyChildren();
              response.data.forEach(shapeData => {
                const { id, color, type, properties } = shapeData;
                console.log(`Drawing shape ${id} of type ${type}`);
                this.drawFromBack(id, color, type, properties);
              });
              this.layer.batchDraw(); 
            })
            .catch(error => {
              console.error('Error loading file:', error);
            });
        } else {
          console.warn('No file path entered.');
          this.showMessage = true;
          this.messageText = "No path is entered!";
          setTimeout(() => {
            this.showMessage = false;
          }, 3000);
        }
      },
      loadFileXML() {
      this.otherVariable = this.filePath.replace(/\\/g, '//');

        if (this.otherVariable) {

          axios.get(`http://localhost:8080/all/xml?path=${this.otherVariable}`)
            .then(response => {
              this.shapes = []; 
              this.layer.destroyChildren();
              response.data.forEach(shapeData => {
                const { id, color, type, properties } = shapeData;
                console.log(`Drawing shape ${id} of type ${type}`);
                this.drawFromBack(id, color, type, properties);
              });
              this.layer.batchDraw(); 
            })
            .catch(error => {
              console.error('Error loading file:', error);
            });
        } else {
          console.warn('No file path entered.');
          this.showMessage = true;
          this.messageText = "No path is entered!";
          setTimeout(() => {
            this.showMessage = false;
          }, 3000);
        }
      },
  showErrorMessage(message) {
    this.errorMessage = message;
    this.showError = true;

    setTimeout(() => {
      this.showError = false;
    }, 1000); 
  },
    enableTransformer(shape) {
      const transformer = new Konva.Transformer();
      this.layer.add(transformer);
      transformer.attachTo(shape);
      this.stage.add(this.layer);
      return transformer;
    },

    disableTransformer() {
      if (this.selectedShape && this.selectedShape.transformer) {
        this.selectedShape.transformer.destroy();
        this.selectedShape.transformer = null;
        this.stage.draw();
      }
    },

    handleShapeClick(shape) {
      if (this.selectedShape && this.selectedShape.id === shape.id) {
        this.disableTransformer();
        this.selectedShape = null;
      } else {
        this.disableTransformer();
        this.selectedShape = shape;
        this.selectedShape.transformer = this.enableTransformer(shape);
      }
    },
    color(shape) {
      shape.on("click", () => {
        if (this.coloring && (this.selectedShape != null)) {
          const selectedColor = this.selectedColor;
          shape.fill(selectedColor);
          shape.stroke(selectedColor);
          this.createDummyBackObj(shape);
          this.layer.batchDraw();
         this.axiosPut(shape);
        }
      });
    },
    copySelectedShape() {
      if(this.selectedShape != null){
        var clonedShape;
          if(this.selectedShape.attrs.type=='circle'){
              clonedShape = new Konva.Circle(this.selectedShape.attrs);
          }
          else if(this.selectedShape.attrs.type=='rectangle'){
            clonedShape = new Konva.Rect(this.selectedShape.attrs);
          }
          else if(this.selectedShape.attrs.type=='line'){
            clonedShape = new Konva.Rect(this.selectedShape.attrs);
          }
          else if(this.selectedShape.attrs.type=='square'){
            clonedShape = new Konva.Rect(this.selectedShape.attrs);
          }
          else if(this.selectedShape.attrs.type=='ellipse'){
            clonedShape = new Konva.Ellipse(this.selectedShape.attrs);
          }
          else if(this.selectedShape.attrs.type=='triangle'){
            clonedShape = new Konva.RegularPolygon(this.selectedShape.attrs);
          }
          clonedShape.id(this.id++);
          clonedShape.x(this.selectedShape.x()+15);
          clonedShape.y(this.selectedShape.y()+15);
          clonedShape.on("click", () => this.handleShapeClick(clonedShape));
          clonedShape.on("transformend",()=>this.handleScaleTransform(clonedShape));
          this.color(clonedShape);
          this.layer.add(clonedShape);
          this.createDummyBackObj(clonedShape);
          this.shapes.push(clonedShape);
          this.layer.batchDraw();
          this.axiosPost(this.selectedShape);
          
         
    }else{
      this.showMessage = true;
      this.messageText = "No shape is selected!";
      setTimeout(() => {
        this.showMessage = false;
      }, 3000);
    }
    },
    deleteSelectedShape2() {
      if (this.selectedShape != null) {
        var index = this.shapes.findIndex((shape) => shape === this.selectedShape);
        if (index !== -1) {
          this.shapes.splice(index, 1);
        }
        this.selectedShape.destroy();
        this.layer.batchDraw();
        this.disableTransformer();
        this.selectedShape = null;
    }else{
      this.showMessage = true;
      this.messageText = "No shape is selected!";
      setTimeout(() => {
        this.showMessage = false;
      }, 3000);
    }
    },
    deleteSelectedShape() {
      if (this.selectedShape != null) {
        var index = this.shapes.findIndex((shape) => shape === this.selectedShape);
        if (index !== -1) {
          this.shapes.splice(index, 1);
        }
        this.createDummyBackObj(this.selectedShape);
        this.selectedShape.destroy();
        this.layer.batchDraw();
        this.disableTransformer();
        this.selectedShape = null;
        axios.delete(`http://localhost:8080/${this.dummyBackObj.id}`, this.dummyBackObj)
         .then(response => {
        console.log('Shape deleted successfully:', response.data);
      })
      .catch(error => {
        console.error('Error deleting shape:', error);
      });
      this.clearDummyBackObj();
    }else{
      this.showMessage = true;
      this.messageText = "No shape is selected!";
      setTimeout(() => {
        this.showMessage = false;
      }, 3000);
     }
    },
     handleScaleTransform(shape){
        const scaleX = shape.scaleX();
        const scaleY = shape.scaleY();
        shape.width(shape.width()*scaleX);
        shape.height(shape.height()*scaleY);
        shape.scaleX(1);
        shape.scaleY(1);
        this.createDummyBackObj(shape);
        this.axiosPut(shape);
    },
    drawRectangle() {
      var newRect = new Konva.Rect({
        type: "rectangle",
        id: this.id,
        x: 50,
        y: 20,
        width: 50,
        height: 30,
        fill: null,
        stroke: "black",
        draggable: true,
      });
      newRect.on("click", () => this.handleShapeClick(newRect));
      newRect.on("transformend",()=>this.handleScaleTransform(newRect));
      newRect.on("dragend", ()=>(this.createDummyBackObj(newRect),this.axiosPut(newRect)));
      this.color(newRect);
      this.layer.add(newRect);
      this.createDummyBackObj(newRect);
      this.id++;
      this.shapes.push(newRect);
      this.axiosPost(newRect);
    },
    drawCircle() {
      var newCircle = new Konva.Circle({
        id: this.id,
        type: "circle",
        x: 200,
        y: 100,
        radius: 50,
        fill: null,
        stroke: "black",
        draggable: true,
      });
      this.id++;
      newCircle.on("click", () => this.handleShapeClick(newCircle));
      newCircle.on("transformend",()=>this.handleScaleTransform(newCircle));
      newCircle.on("dragend", ()=>(this.createDummyBackObj(newCircle),this.axiosPut(newCircle)));
      this.color(newCircle);
      this.createDummyBackObj(newCircle);
      this.layer.add(newCircle);
      this.stage.add(this.layer);
      this.shapes.push(newCircle);
      this.axiosPost(newCircle);
    },
    drawEllipse() {
      var oval = new Konva.Ellipse({
        id: this.id,
        type: "ellipse",
        x: this.stage.width() / 2,
        y: this.stage.height() / 2,
        radiusX: 100,
        radiusY: 50,
        fill: null,
        stroke: "black",
        draggable: true,
      });
      this.id++;
      oval.on("click", () => this.handleShapeClick(oval));
        oval.on("transformend",()=>this.handleScaleTransform(oval));
      oval.on("dragend", ()=>(this.createDummyBackObj(oval),this.axiosPut(oval)));
      this.color(oval);
      this.createDummyBackObj(oval);
      this.layer.add(oval);
      this.stage.add(this.layer);
      this.shapes.push(oval);
      this.axiosPost(oval);
    },
    drawLineSegment() {
       var line = new Konva.Rect({
        draggable: true,
        type: "line",
        id: this.id,
        x:50,
        y:50,
        width:200,
        height:1,
        fill:"black",
        stroke: "black",
        strokeWidth: 3,
        lineCap: "round",
        lineJoin: "round",
        });
      this.id++;
      line.on("click", () => this.handleShapeClick(line));
        line.on("transformend",()=>this.handleScaleTransform(line));
      line.on("dragend", ()=>(this.createDummyBackObj(line),this.axiosPut(line)));
      this.color(line);
      this.createDummyBackObj(line);
      this.layer.add(line);
      this.stage.add(this.layer);
      this.shapes.push(line);
      this.axiosPost(line);
    },
    drawSquare() {
      var newSquare = new Konva.Rect({
        id: this.id,
        type: "square",
        x: 80,
        y: 30,
        width: 50,
        height: 50,
        fill: null,
        stroke: "black",
        strokeWidth: 2,
        draggable: true,
      });
      this.id++;
      newSquare.on("click", () => this.handleShapeClick(newSquare));
      newSquare.on("transformend",()=>this.handleScaleTransform(newSquare));
      newSquare.on("dragend", ()=>(this.createDummyBackObj(newSquare),this.axiosPut(newSquare)));
      this.color(newSquare);
      this.createDummyBackObj(newSquare);
      this.layer.add(newSquare);
      this.stage.add(this.layer);
      this.shapes.push(newSquare);
      this.axiosPost(newSquare);
    },
    drawTriangle() {
      var newTriangle = new Konva.RegularPolygon({
        id: this.id,
        type: "triangle",
        x: 150,
        y: 100,
        sides: 3,
        radius: 50,
        fill: null,
        rotation: 0,
        stroke: "black",
        draggable: true,
      });
      this.id++;
      newTriangle.on("click", () => this.handleShapeClick(newTriangle));
      newTriangle.on("dragend", ()=>(this.createDummyBackObj(newTriangle),this.axiosPut(newTriangle)));
      this.color(newTriangle);
      this.createDummyBackObj(newTriangle);
      this.layer.add(newTriangle);
      this.stage.add(this.layer);
      this.shapes.push(newTriangle);
      this.axiosPost(newTriangle);
    },
    axiosPut(shape){
        axios.put(`http://localhost:8080/${shape.attrs.type}`, this.dummyBackObj)
         .then(response => {
           console.log('shape saved , updated successfully:', response.data);
         })
         .catch(error => {
           console.error('Error saving, updating shape:', error);
         });
         this.clearDummyBackObj();
    },
    axiosPost(shape){
      axios.post(`http://localhost:8080/${shape.attrs.type}`, this.dummyBackObj)
        .then(response => {
          console.log('Shape saved successfully:', response.data);
        })
        .catch(error => {
          console.error('Error saving shape:', error);
        });
        this.clearDummyBackObj();
    },
    redo(){
      axios.get('http://localhost:8080/redo')
      .then(response => {
      var loaded = response.data;

      var existing =this.getShapeById(loaded.id);
      if(existing !== null){
        this.selectedShape = existing;
        this.deleteSelectedShape2();
      }
      this.drawFromBack(loaded.id,loaded.color,loaded.type,loaded.properties);
      console.log('Redone successfully:', response.data); 
    }).catch(error => {
          console.error('Error redoing:', error);
          });
    },
    undo(){
      axios.get('http://localhost:8080/undo')
      .then(response => {
        var loaded = response.data;
       
      var existing = this.getShapeById(loaded.id);
      this.createDummyBackObj(existing);
      this.selectedShape = existing;
      this.deleteSelectedShape2();

       if(!this.compareExisitingShape(loaded,this.dummyBackObj)){
        this.drawFromBack(loaded.id,loaded.color,loaded.type,loaded.properties);
          console.log("shape is drawed");
       }
        console.log('Undone successfully:', response.data);  
      }).catch(error => {
          console.error('Error undoing:', error);
          });
        this.clearDummyBackObj();
    },
 createDummyBackObj(shape){
    if(shape!=null){
      
      this.dummyBackObj.properties = [];
      this.dummyBackObj.id= shape.id();
      if(shape.fill()===undefined)
             this.dummyBackObj.color = null;
      else
             this.dummyBackObj.color = shape.fill();
      this.dummyBackObj.type = shape.attrs.type;
      this.dummyBackObj.properties.push(shape.rotation(), shape.x(), shape.y() );
      
        switch(shape.attrs.type){
          case 'triangle':
          case 'circle':
            this.dummyBackObj.properties.push(shape.radius());
            break;
          case 'line':
          case 'square':
          case 'rectangle':
            this.dummyBackObj.properties.push(shape.width(), shape.height());
            break;
          case 'ellipse':
            this.dummyBackObj.properties.push(shape.radiusX(), shape.radiusY());
            break; 
        }
        this.dummyBackObj.type = shape.attrs.type;
    }

  },
  clearDummyBackObj(){
    this.dummyBackObj.id = null;
    this.dummyBackObj.color =null;
    this.dummyBackObj.type = null;
    this.dummyBackObj.properties=[];
  },
  compareExisitingShape(loaded,comp) {
  console.log("properties1 "+comp.properties);
  console.log("properties2 "+loaded.properties);
  console.log("Type1 "+comp.type+" color1 "+comp.color);
  console.log("Type2 "+loaded.type+" color2 "+loaded.color);

  if (
    comp.color === loaded.color &&
    comp.properties.length === loaded.properties.length 
  ) {
      if (JSON.stringify(comp.properties) !== JSON.stringify(loaded.properties)) {
        return false; 
      }
    return true; 
    }
  return false; 
},
  getShapeById(id) {
  for (const child of this.layer.children) {
    if (child.id() === id) {
      console.log('Shape found!');
      console.log('Child id:', child.id());
      return child;
    }
  }
  console.log('Shape not found!');
  return null;
  },
  drawFromBack(id, color, type, properties) {
      let newShape, strokeColor;
      console.log(color);
      if(color === null || color === undefined){
          strokeColor = "black";
          console.log("here");
      }
      switch (type) {
        case 'rectangle':
          console.log("here");
          newShape = new Konva.Rect({
            id: id,
            type: 'rectangle',
            x: properties[1],
            y: properties[2],
            rotation: properties[0],
            width: properties[3],
            height: properties[4],
            fill: color,
            stroke: strokeColor,
            draggable: true,
          });
          break;
        case 'circle':
          newShape = new Konva.Circle({
            id: id,
            type: 'circle',
            x: properties[1],
            y: properties[2],
            radius: properties[3],
            rotation: properties[0],
            fill: color,
            stroke: strokeColor,
            draggable: true,
          });
          break;
        case 'ellipse':
          newShape = new Konva.Ellipse({
            id: id,
            type: 'ellipse',
            x: properties[1],
            y: properties[2],
            radiusX: properties[3],
            radiusY: properties[4],
            rotation: properties[0],
            fill: color,
            stroke: strokeColor,
            draggable: true,
          });
          break;
          case 'line':
          newShape = new Konva.Rect({
            draggable: true,
            type: "line",
            id: id,
            x:properties[1],
            y:properties[2],
            width:properties[3],
            height:properties[4],
            rotation: properties[0],
            fill:color,
            stroke: strokeColor,
            strokeWidth: 3,
            lineCap: "round",
            lineJoin: "round",
            });
            break;
          case 'square':
          newShape = new Konva.Rect({
            id: id,
            type: "square",
            x:properties[1],
            y:properties[2],
            width:properties[3],
            height:properties[4],
            rotation: properties[0],
            fill: color,
            stroke: strokeColor,
            strokeWidth: 2,
            draggable: true,
          });
          break;
          case 'triangle':
          newShape = new Konva.RegularPolygon({
            id: id,
            type: "triangle",
            x: properties[1],
            y: properties[2],
            sides: 3,
            radius: properties[3],
            rotation: properties[0],
            fill: color,
            stroke: strokeColor,
            draggable: true,
          });
          break;

        default:
          console.error('Unknown shape type:', type);
          return;
  }
  newShape.on('click', () => this.handleShapeClick(newShape));
  newShape.on('transformend', () => this.handleScaleTransform(newShape));
  newShape.on('dragend', () => (this.createDummyBackObj(newShape), this.axiosPut(newShape)));
  this.color(newShape);
  this.createDummyBackObj(newShape);
  this.layer.add(newShape);
  this.stage.add(this.layer);
  this.shapes.push(newShape);  
},
},
};

</script>

<style>
  body {
    margin: 0;
    padding: 0;
    font-family: 'Arial', sans-serif;
  }
  .hello {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 95vw;
    height: 95vh;
    background-color: rgb(47, 47, 47);
    padding: 20px;
  }

  .ToolBox {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-evenly;
  }

 #saveLoad{
  flex-direction: row;
    align-items: center;
    background-color: rgb(47, 47, 47);
    padding-left:100px;
 }
 #saveLoad button{
  width: 10%;
 }
  button {
    width: 100%;
    margin: 10px;
    padding: 15px;
    background-color:rgb(108, 31, 179);
    color: #000000;
    border: none;
    cursor: pointer;
    border-radius: 5px;
    transition: background-color 0.3s ease;
    text-align: center; 
  }

  button:hover {
    background-color:rgb(67, 13, 117);
  }

  .button-clicked {
    background-color: #e74c3c;
  }

  input[type="color"] {
    margin: 10px;
    width: 100%;
    border-radius: 5px;
  }

  .konvaHolder {
    flex: 1;
    margin: 10px;
    border: 2px solid rgb(161, 161, 161);
    border-radius: 5px;
    overflow: hidden;
    background-color:aliceblue;
    box-shadow: 5px 5px #000000;
  }
  .message {
  position: fixed;
  bottom: 20px; 
  left: 50%;
  transform: translateX(-50%);
  background-color: #4CAF50;
  color: white;
  padding: 15px;
  border-radius: 5px;
  display: none;
}
  .show-message {
  position: fixed;
  bottom: 20px; 
  left: 50%;
  transform: translateX(-50%);
  background-color: #4CAF50;
  color: white;
  padding: 15px;
  border-radius: 5px;
  display: block;
  opacity: 1;
  transition: opacity 0.3s ease;
}
  input[type="text"] {
    width: 20%;
    margin: 5px;
    padding: 8px;
    border: 3px solid #ccc;
    border-radius: 5px;
    transition: black 0.3s ease;
  }

  input[type="text"]:focus {
    border-color: #3498db;
  }
</style>


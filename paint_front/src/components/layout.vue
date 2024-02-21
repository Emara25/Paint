<template>

  <div class="hello">
    <div class="ToolBox">
      <button v-for="button in left_buttons" :key="button.value" @click="handleButtonClick(button)" >
          {{ button.label }}
        </button>
        <input type="color" v-model="selectedColor" />
    </div>
    <div class="konvaHolder">
      <v-stage
      listening = "true"
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
      <button v-for="button in right_buttons" 
      :key="button.value" @click="handleButtonClick(button)" 
      :class="{ 'button-clicked': button.value ==='color' &&ButtonClicked }">
  {{ button.label }}
</button>

      <div class="file-input-container">
      <input type="text" v-model="filePath" placeholder="Enter file name to load">
      <input type="text" v-model="fileName" placeholder="Enter file name to save">
      
      </div>
  
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
      { label: "Line", value: "line", imagePath: "../assets/circle.png" },//post/{type} {id,color,properities[]}
        { label: "Circle", value: "circle" },
        { label: "Ellipse", value: "ellipse" },
        { label: "Square", value: "square" },
        { label: "Rectangle", value: "rectangle" },
        { label: "Triangle", value: "triangle" },
      ],

      right_buttons: [
       { label: "Color", value: "color" }, //put/id/ {id,color,properities[]}
        { label: "Copy", value: "copy" }, //post/{type}    {id,color,properities[]}
        { label: "Delete", value: "delete" }, //put/id
        { label: "Save", value: "save" }, //post/all/json
        { label: "Load", value: "load" }, //get/all/json  response{array of objects}
        { label: "Undo", value: "undo" }, // get/undo  response{object}
        { label: "Redo", value: "redo" }, // get/redo  response{object}
        { label: "print", value: "info" },
      ],
      
      shapes: [],

      selectedShape: null,
      selectedColor: "#000000", // Initial color
      fill: null,
      id: 1,
      isDrawing: false,
      coloring: false,
      ButtonClicked: false,
      showMessage: false,
      messageText: "",
      undoStack: [],
      redoStack: [],
      filePath: "",
      fileName: "",

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

      if (value === "rectangle") {
        this.drawRectangle();
        this.stage.add(this.layer);
      } else if (value === "circle") {
        this.drawCircle();
        this.stage.add(this.layer);
      } else if (value === "ellipse") {
        this.drawEllipse();
        this.stage.add(this.layer);
      } else if (value === "line") {
        this.drawLineSegment();
        this.stage.add(this.layer);
      } else if (value === "square") {
        this.drawSquare();
        this.stage.add(this.layer);
      } else if (value === "triangle") {
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
      } else if(value === "save"){
        const savePath = this.fileName.trim()+'.json';
        axios.post(`http://localhost:8080/all/json?path=${savePath}`)
        .then(response => {
          console.log('File saved successfully:', response.data);
        })
        .catch(error => {
          console.error('Error saving File:', error);
        });
      } else if(value ==="load"){
        this.loadFile();
      }else if(value === "undo"){
        this.undo();
      }else if(value === "redo"){
          this.redo();
      }
    },
    loadFile() {
        const filePath = this.filePath.trim()+'.json';

        if (filePath) {
          const url = `http://localhost:8080/all/json?path=${filePath}`;

          axios.get(url)
            .then(response => {
              this.shapes = []; // Clear existing shapes
              this.layer.destroyChildren();
              response.data.forEach(shapeData => {
                const { id, color, type, properties } = shapeData;
                console.log(`Drawing shape ${id} of type ${type}`);
                this.drawFromBack(id, color, type, properties);
              });
              this.layer.batchDraw(); // Redraw the layer after loading shapes
            })
            .catch(error => {
              console.error('Error loading file:', error);
            });
        } else {
          console.warn('No file path entered.');
        }
      },

  // Method to show an error message
  showErrorMessage(message) {
    this.errorMessage = message;
    this.showError = true;

    // Optionally, hide the error message after a certain duration
    setTimeout(() => {
      this.showError = false;
    }, 5000); // Adjust the duration as needed
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
        // If the same shape is clicked again, toggle the Transformer off
        this.disableTransformer();
        this.selectedShape = null;
      } else {
        // Otherwise, disable the existing Transformer (if any)
        this.disableTransformer();

        // Enable the Transformer for the clicked shape
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
      // Clone the selected shape
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
      // Add the new rectangle to the shapes array
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
    //handleShapeDragStart(shape) {
      // Handle drag start event
    //console.log('Shape ${shape.x} + ${shape.y} started dragging.');
 // },
  
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
          case 'rectagle':
            this.dummyBackObj.properties.push(shape.width(), shape.height());
            break;
          case 'ellipse':
            this.dummyBackObj.properties.push(shape.radiusX(), shape.radiusY());
            break; 
        }
        //console.log(this.dummyBackObj);
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

  // Ensure that the type, id, and color match
  if (
    comp.color === loaded.color &&
    comp.properties.length === loaded.properties.length 
  ) {
    // Compare each property individually
      if (JSON.stringify(comp.properties) !== JSON.stringify(loaded.properties)) {
        return false; // Properties do not match 
      }
    return true; // All properties match
    }
  return false; // Type, id, or color do not match
},
  getShapeById(id) {
  // Iterate over the children of the layer
  for (const child of this.layer.children) {
    // Check if the current child has an 'id' property
    if (child.id() === id) {
      // If found, return the shape
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

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
body {
  color: rgba(105, 105, 105, 0.392);
}

.hello {
  display: flex;
  width: 95vw;
  height: 95vh;

}

.ToolBox {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: 10%;
  height: 100%;
  box-sizing: border-box;
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
  }

button {
  width: 100%;
  height: 70px;
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  border-radius: 3px;
  font-size: 20px;
}

button:hover {
  background-color: #f0f0f034;
}

.konvaHolder {
  flex: 1;
  background-color: white;
  box-shadow: 10px 10px 26px red ;
  max-width: 80%;
  height: 100%;
  box-sizing: border-box;
  border: 3px solid rgb(0, 0, 0);
  overflow: hidden; 
}
.button-clicked {
  background-color: rgb(0, 30, 255);
  border: 4px solid #000000;
  }
.button-clicked:hover {
  background-color: rgba(92, 93, 113, 0.34);
  }

</style>

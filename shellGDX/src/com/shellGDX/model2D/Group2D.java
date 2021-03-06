package com.shellGDX.model2D;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.shellGDX.shader.ShaderInstance;

public class Group2D extends Group
{
  public ShaderInstance shader = null;
  protected int zIndex = 0;

  public Group2D()
  {
    this(0, 0, 0, 1, 1);
  }

  public Group2D(float posX, float posY)
  {
    this(posX, posY, 0, 1, 1);
  }

  public Group2D(float posX, float posY, float degrees)
  {
    this(posX, posY, degrees, 1, 1);
  }

  public Group2D(float posX, float posY, float degrees, float scaleX, float scaleY)
  {
    super();
    setTouchEnable(true);
    setPosition(posX, posY);
    setRotation(degrees);
    setScale(scaleX, scaleY);
  }
  
  //position
  public void setPosition(Vector2 position)
  {
    setPosition(position.x, position.y);
  }

  public Vector2 getPosition()
  {
    return new Vector2(getX(), getY());
  }
  
  //scale
  public void setScale(Vector2 scale)
  {
    setScale(scale.x, scale.y);
  }

  public Vector2 getScale()
  {
    return new Vector2(getScaleX(), getScaleY());
  }
  
  public Vector2 screenToLocalCoordinates(float screenX, float screenY)
  {
    Vector2 buffer = new Vector2(screenX, screenY);
    buffer = screenToLocalCoordinates(buffer);
    return buffer;
  }
  
  @Override
  public void setZIndex(int zIndex)
  {
    this.zIndex = zIndex;
  }

  @Override
  public int getZIndex()
  {
    return this.zIndex;
  }
  
  protected Rectangle bound = new Rectangle(0, 0, 0, 0);
  
  @Override
  public float getWidth()
  {
    return bound.width;
  }
  
  @Override
  public float getHeight()
  {
    return bound.height;
  }

  public Rectangle getBound()
  {
    return bound;
  }
  
  public void updateBound()
  {
    bound.set(0, 0, 0, 0);
    
    SnapshotArray<Actor> children = getChildren();
    if (children.size <= 0)
      return;

    bound.set(getBoundChild(children.get(0)));
    
    Actor[] actors = children.begin();
    for (int i = 1, n = children.size; i < n; i++)
    {
      bound.merge(getBoundChild(actors[i]));
    }
    children.end();
  }
  
  protected Rectangle getBoundChild(Actor child)
  {
    if (child instanceof Group2D)
    {
      ((Group2D)child).updateBound();
      return ((Group2D)child).getBound();
    }
    
    if (child instanceof ModelObject2D)
    {
      ((ModelObject2D)child).updateBound();
      return ((ModelObject2D)child).getBound();
    }
    
    return new Rectangle(0, 0, 0, 0);
  }
  
  @Override
  public void addActor(Actor actor)
  {
    Array<Actor> children = getChildren();
    for (int i = 0; i < children.size; i++)
    {
      if (actor.getZIndex() < children.get(i).getZIndex())
      {
        super.addActorAt(i, actor);
        return;
      }
    }
    super.addActor(actor);
  }
  
  @Override
  public void setVisible(boolean visible)
  {
    super.setVisible(visible);
    
    SnapshotArray<Actor> children = getChildren();
    if (children.size <= 0)
      return;
    
    Actor[] actors = children.begin();
    for (int i = 0, n = children.size; i < n; i++)
      actors[i].setVisible(visible);
    children.end();
  }

  public boolean update(float deltaTime)
  {
    return isVisible();
  }
  
  protected Scene2D scene = null;
  
  @Override
  protected void setStage(Stage stage)
  {
    super.setStage(stage);
    if (stage instanceof Scene2D)
      scene = (Scene2D)stage;
  }
  
  @Override
  public void act(float delta)
  {
    super.act(delta);
    update(delta);
  }
  
  @Override
  public void draw(Batch batch, float parentAlpha)
  {
    if (scene == null)
      return;

    if (bound.width > 0.0f &&
        bound.height > 0.0f &&
        !scene.getCameraRectangle().overlaps(bound))
      return;

    super.draw(batch, parentAlpha);
  }

  //touch
  protected InputListener inputListner = null;
  
  public void setTouchEnable(boolean enable)
  {
    for(Actor child : getChildren())
      child.setTouchable(enable ? Touchable.enabled : Touchable.disabled);
    
    if (enable)
    {
      setTouchable(Touchable.enabled);
      
      inputListner = new InputListener()
      {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
          return Group2D.this.touchDown(event, x, y, pointer, button);
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
          Group2D.this.touchUp(event, x, y, pointer, button);
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
          Group2D.this.touchDragged(event, x, y, pointer);
        }

        @Override
        public boolean mouseMoved(InputEvent event, float x, float y) {
          return Group2D.this.mouseMoved(event, x, y);
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
          Group2D.this.enter(event, x, y, pointer, fromActor);
        }

        @Override
        public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
          Group2D.this.exit(event, x, y, pointer, toActor);
        }

        @Override
        public boolean scrolled(InputEvent event, float x, float y, int amount) {
          return Group2D.this.scrolled(event, x, y, amount);
        }

        @Override
        public boolean keyDown(InputEvent event, int keycode) {
          return Group2D.this.keyDown(event, keycode);
        }

        @Override
        public boolean keyUp(InputEvent event, int keycode) {
          return Group2D.this.keyUp(event, keycode);
        }

        @Override
        public boolean keyTyped(InputEvent event, char character) {
          return Group2D.this.keyTyped(event, character);
        }
      };
      
      addListener(inputListner);
      return;
    }
    
    setTouchable(Touchable.disabled);
    removeListener(inputListner);
    inputListner = null;
  }
  
  public boolean getTouchEnable()
  {
    return getTouchable() == Touchable.enabled;
  }

  public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return false; }
  public void touchUp(InputEvent event, float x, float y, int pointer, int button) {}
  public void touchDragged(InputEvent event, float x, float y, int pointer) {}
  public boolean mouseMoved(InputEvent event, float x, float y) { return false; }

  public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {}
  public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {}
  public boolean scrolled(InputEvent event, float x, float y, int amount) { return false; }

  public boolean keyDown(InputEvent event, int keycode) { return false; }
  public boolean keyUp(InputEvent event, int keycode) { return false; }
  public boolean keyTyped(InputEvent event, char character) { return false; }
}

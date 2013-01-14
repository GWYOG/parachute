//
// This work is licensed under the Creative Commons
// Attribution-ShareAlike 3.0 Unported License. To view a copy of this
// license, visit http://creativecommons.org/licenses/by-sa/3.0/
//

package parachute.client;

import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

public class ParachuteModelRenderer {
	
	private PositionTextureVertex corners[];
	private ParachuteTexturedQuad faces[];
	private int left;
	private int top;
	public float rotationPointX;
	public float rotationPointY;
	public float rotationPointZ;
	public float rotateAngleX;
	public float rotateAngleY;
	public float rotateAngleZ;
	private boolean compiled;
	private int displayList;
	public boolean mirror;
	public boolean showModel;

	public ParachuteModelRenderer(int x, int y) {
		compiled = false;
		displayList = 0;
		mirror = false;
		showModel = true;
		left = x;
		top = y;
	}

	public void addBox(float x, float y, float z, int i, int j, int k) {
		corners = new PositionTextureVertex[8];
		faces = new ParachuteTexturedQuad[6];

		float width = x + (float) i;
		float height = y + (float) j;
		float depth = z + (float) k;

		if (mirror) {
			float tmp = width;
			width = x;
			x = tmp;
		}

		corners[0] = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
		corners[1] = new PositionTextureVertex(width, y, z, 0.0F, 8F);
		corners[2] = new PositionTextureVertex(width, height, z, 8F, 8F);
		corners[3] = new PositionTextureVertex(x, height, z, 8F, 0.0F);
		corners[4] = new PositionTextureVertex(x, y, depth, 0.0F, 0.0F);
		corners[5] = new PositionTextureVertex(width, y, depth, 0.0F, 8F);
		corners[6] = new PositionTextureVertex(width, height, depth, 8F, 8F);
		corners[7] = new PositionTextureVertex(x, height, depth, 8F, 0.0F);

		// sides may be smaller than 16, need to account for that.
		int r1 = (i > 16) ? 16 : i;
		int r2 = (k > 16) ? 16 : k;
		int bottom = (j > 16) ? 16 : j;

		faces[0] = new ParachuteTexturedQuad(
				new PositionTextureVertex[] { // right face
				corners[5], corners[1], corners[2], corners[6] }, left, top, left + r1, top + bottom);

		faces[1] = new ParachuteTexturedQuad(
				new PositionTextureVertex[] { // left face
				corners[0], corners[4], corners[7], corners[3] }, left, top, left + r1, top + bottom);

		faces[2] = new ParachuteTexturedQuad(
				new PositionTextureVertex[] { // top face
				corners[5], corners[4], corners[0], corners[1] }, left, top, left + r1, top + r2);

		faces[3] = new ParachuteTexturedQuad(
				new PositionTextureVertex[] { // bottom face
				corners[2], corners[3], corners[7], corners[6] }, left, top, left + r1, top + r2);

		faces[4] = new ParachuteTexturedQuad(
				new PositionTextureVertex[] { // back face
				corners[1], corners[0], corners[3], corners[2] }, left, top, left + r1, top + bottom);

		faces[5] = new ParachuteTexturedQuad(
				new PositionTextureVertex[] { // front face
				corners[4], corners[5], corners[6], corners[7] }, left, top, left + r1, top + bottom);

		if (mirror) {
			for (int l = 0; l < faces.length; l++) {
				faces[l].flipFace();
			}
		}
	}

	public void updateTextureCoords(int left, int top, int w, int h, int d) {
		int r1 = (w > 16) ? 16 : w;
		int r2 = (d > 16) ? 16 : d;
		int bottom = (h > 16) ? 16 : h;

		faces[0].updateTextureCoords(left, top, left + r1, top + bottom);
		faces[1].updateTextureCoords(left, top, left + r1, top + bottom);
		faces[2].updateTextureCoords(left, top, left + r1, top + r2);
		faces[3].updateTextureCoords(left, top, left + r1, top + r2);
		faces[4].updateTextureCoords(left, top, left + r1, top + bottom);
		faces[5].updateTextureCoords(left, top, left + r1, top + bottom);
		compiled = false;
	}

	public void setRotationPoint(float x, float y, float z) {
		rotationPointX = x;
		rotationPointY = y;
		rotationPointZ = z;
	}

	public void render(float f) {
		if (!showModel) {
			return;
		}
		if (!compiled) {
			compileDisplayList(f);
		}
		if (rotateAngleX != 0.0F || rotateAngleY != 0.0F || rotateAngleZ != 0.0F) {
			GL11.glPushMatrix();
			GL11.glTranslatef(rotationPointX * f, rotationPointY * f, rotationPointZ * f);
			if (rotateAngleZ != 0.0F) {
				GL11.glRotatef(rotateAngleZ * 57.29578F, 0.0F, 0.0F, 1.0F);
			}
			if (rotateAngleY != 0.0F) {
				GL11.glRotatef(rotateAngleY * 57.29578F, 0.0F, 1.0F, 0.0F);
			}
			if (rotateAngleX != 0.0F) {
				GL11.glRotatef(rotateAngleX * 57.29578F, 1.0F, 0.0F, 0.0F);
			}
			GL11.glCallList(displayList);
			GL11.glPopMatrix();
		} else if (rotationPointX != 0.0F || rotationPointY != 0.0F || rotationPointZ != 0.0F) {
			GL11.glTranslatef(rotationPointX * f, rotationPointY * f, rotationPointZ * f);
			GL11.glCallList(displayList);
			GL11.glTranslatef(-rotationPointX * f, -rotationPointY * f, -rotationPointZ * f);
		} else {
			GL11.glCallList(displayList);
		}
	}

	public void renderWithRotation(float f) {
		if (!showModel) {
			return;
		}
		if (!compiled) {
			compileDisplayList(f);
		}
		GL11.glPushMatrix();
		GL11.glTranslatef(rotationPointX * f, rotationPointY * f, rotationPointZ * f);
		if (rotateAngleY != 0.0F) {
			GL11.glRotatef(rotateAngleY * 57.29578F, 0.0F, 1.0F, 0.0F);
		}
		if (rotateAngleX != 0.0F) {
			GL11.glRotatef(rotateAngleX * 57.29578F, 1.0F, 0.0F, 0.0F);
		}
		if (rotateAngleZ != 0.0F) {
			GL11.glRotatef(rotateAngleZ * 57.29578F, 0.0F, 0.0F, 1.0F);
		}
		GL11.glCallList(displayList);
		GL11.glPopMatrix();
	}

	public void postRender(float f) {
		if (!showModel) {
			return;
		}
		if (!compiled) {
			compileDisplayList(f);
		}
		if (rotateAngleX != 0.0F || rotateAngleY != 0.0F || rotateAngleZ != 0.0F) {
			GL11.glTranslatef(rotationPointX * f, rotationPointY * f, rotationPointZ * f);
			if (rotateAngleZ != 0.0F) {
				GL11.glRotatef(rotateAngleZ * 57.29578F, 0.0F, 0.0F, 1.0F);
			}
			if (rotateAngleY != 0.0F) {
				GL11.glRotatef(rotateAngleY * 57.29578F, 0.0F, 1.0F, 0.0F);
			}
			if (rotateAngleX != 0.0F) {
				GL11.glRotatef(rotateAngleX * 57.29578F, 1.0F, 0.0F, 0.0F);
			}
		} else if (rotationPointX != 0.0F || rotationPointY != 0.0F || rotationPointZ != 0.0F) {
			GL11.glTranslatef(rotationPointX * f, rotationPointY * f, rotationPointZ * f);
		}
	}

	private void compileDisplayList(float f) {
		displayList = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(displayList, GL11.GL_COMPILE);
		Tessellator tessellator = Tessellator.instance;

		for (int i = 0; i < faces.length; i++) {
			faces[i].draw(tessellator, f);
		}

		GL11.glEndList();
		compiled = true;
	}

}
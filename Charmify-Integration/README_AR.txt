Charmify Integration - NetBeans Maven Project

Open NetBeans > File > Open Project > select Charmify-Integration.
Run StoreGUI.java to open the store.

Includes StoreGUI, ProductCard, CartFrame, Inventory, Product, Category,
DynamicPhoneCasePreviewPanel, ImageManager, StickerItem, and image resources.

IMPORTANT: Original Cart.java source was not supplied as a standalone file.
This package includes a replacement Cart.java implementing the required API.
Please compare it with the original Cart.java before merging to the team project.
The ClientLoginWindow.java file and server/network files are not integrated.
This package tests local GUI/cart integration, NOT complete client-server operation.

All Java classes use the default package because the provided sources do.
StoreGUI and CartFrame share one Inventory object.

"use client";

import AdminLayout from "@/components/admin/AdminLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import {
  Brand,
  Category,
  PRODUCT_STATUSES,
  Product,
  UpdateProductRequest,
} from "@/interfaces/product.interface";
import { productService } from "@/services";
import { useUserStore } from "@/stores/user.store";
import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowLeft, Loader2, Plus, Trash2 } from "lucide-react";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState, useRef } from "react";
import { useFieldArray, useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

const productSchema = z.object({
  id: z.number().min(1, "Product ID is required"),
  name: z.string().min(1, "Product name is required"),
  price: z.number().min(0, "Price must be greater than or equal to 0"),
  quantity: z.number().min(0, "Quantity must be greater than or equal to 0"),
  status: z.enum(PRODUCT_STATUSES),
  availableFrom: z.string().min(1, "Available from date is required"),
  isActive: z.boolean(),
  brandId: z.number().min(1, "Please select a brand"),
  categoryIds: z
    .array(z.number())
    .min(1, "Please select at least one category"),
  image: z.string().optional(),
  details: z.object({
    description: z.string().min(1, "Description is required"),
  }),
  attributes: z.array(
    z.object({
      id: z.number().optional(),
      name: z.string().min(1, "Attribute name is required"),
      value: z.string().min(1, "Attribute value is required"),
    })
  ),
});

type ProductFormData = z.infer<typeof productSchema>;

export default function AdminProductEditPage() {
  const router = useRouter();
  const params = useParams();
  const productId = parseInt(params.id as string, 10);
  const { profile } = useUserStore();
  const [brands, setBrands] = useState<Brand[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [isLoadingBrands, setIsLoadingBrands] = useState(true);
  const [isLoadingCategories, setIsLoadingCategories] = useState(true);
  const [isLoadingProduct, setIsLoadingProduct] = useState(true);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [product, setProduct] = useState<Product | null>(null);
  const hasAuthChecked = useRef(false);
  const hasLoadedData = useRef(false);

  const form = useForm<ProductFormData>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      name: "",
      price: 0,
      quantity: 0,
      status: "IN_STOCK",
      availableFrom: "",
      isActive: true,
      brandId: 0,
      categoryIds: [],
      image: "",
      details: {
        description: "",
      },
      attributes: [],
    },
  });

  const { fields, append, remove, replace } = useFieldArray({
    control: form.control,
    name: "attributes",
  });

  useEffect(() => {
    const initializeComponent = async () => {
      // Check authentication only once
      if (!hasAuthChecked.current) {
        if (!profile || profile.role !== "ADMIN") {
          router.push("/login");
          return;
        }
        hasAuthChecked.current = true;
      }

      // Load data only once
      if (hasLoadedData.current || !productId || !profile || profile.role !== "ADMIN") {
        return;
      }
      
      hasLoadedData.current = true;

      try {
        const [brandsResponse, categoriesResponse, productResponse] =
          await Promise.all([
            productService.listBrand(),
            productService.listCategory(),
            productService.adminGetProduct(productId),
          ]);

        if (brandsResponse.success) {
          setBrands(brandsResponse.data);
        }
        if (categoriesResponse.success) {
          setCategories(categoriesResponse.data);
        }
        if (productResponse.success) {
          const productData = productResponse.data;
          setProduct(productData);

          // Format the date for datetime-local input
          const availableFromDate = new Date(productData.availableFrom);
          const formattedDate = availableFromDate.toISOString().slice(0, 16);

          // Set form values
          form.reset({
            id: productData.id,
            name: productData.name,
            price: productData.price,
            quantity: productData.quantity,
            status: productData.status,
            availableFrom: formattedDate,
            isActive: productData.isActive,
            brandId: productData.brand?.id || 0,
            categoryIds: productData.categories?.map((cat) => cat.id) || [],
            image: productData.image || "",
            details: {
              description: productData.details?.description || "",
            },
            attributes: productData.attributes || [],
          });

          // Set image preview
          if (productData.image) {
            setImagePreview(productData.image);
          }

          // Replace attributes array
          replace(productData.attributes || []);
        } else {
          toast.error("Product not found");
          router.push("/admin/product");
        }
      } catch (error) {
        toast.error("Failed to load product data");
        router.push("/admin/product");
      } finally {
        setIsLoadingBrands(false);
        setIsLoadingCategories(false);
        setIsLoadingProduct(false);
      }
    };

    initializeComponent();
  }, [productId, form, replace, router, profile]);

  const handleImageUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        const result = e.target?.result as string;
        setImagePreview(result);
        form.setValue("image", result);
      };
      reader.readAsDataURL(file);
    }
  };

  const addAttribute = () => {
    append({ id: undefined, name: "", value: "" });
  };

  const removeAttribute = (index: number) => {
    remove(index);
  };

  const onSubmit = async (data: ProductFormData) => {
    try {
      // Filter out empty attributes
      const filteredAttributes = data.attributes
        .filter((attr) => attr.name.trim() && attr.value.trim())
        .map((attr, index) => ({
          id: attr.id || 0, // Use existing ID or 0 for new attributes
          name: attr.name,
          value: attr.value,
        }));

      const formData: UpdateProductRequest = {
        ...data,
        attributes: filteredAttributes,
        availableFrom: new Date(data.availableFrom).toISOString(),
      };

      const response = await productService.adminUpdateProduct(formData);

      if (response.success) {
        toast.success("Product updated successfully!");
        router.push(`/admin/product/${productId}`);
      } else {
        toast.error(response.errorMessage || "Failed to update product");
      }
    } catch (error) {
      toast.error("Failed to update product");
    }
  };

  const formatStatusLabel = (status: string): string => {
    return status
      .split("_")
      .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
      .join(" ");
  };

  if (isLoadingProduct) {
    return (
      <AdminLayout>
        <div className="container mx-auto px-6 py-8">
          <div className="flex items-center justify-center h-64">
            <Loader2 className="h-8 w-8 animate-spin" />
            <span className="ml-2">Loading product...</span>
          </div>
        </div>
      </AdminLayout>
    );
  }

  if (!product) {
    return (
      <AdminLayout>
        <div className="container mx-auto px-6 py-8">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-gray-900 mb-4">
              Product Not Found
            </h1>
            <p className="text-gray-600 mb-4">
              The product you're looking for doesn't exist.
            </p>
            <Button onClick={() => router.push("/admin/product")}>
              Back to Products
            </Button>
          </div>
        </div>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      <div className="container mx-auto px-2">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <Button onClick={() => router.back()} variant="outline">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back
          </Button>
        </div>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            {/* Basic Information Card */}
            <Card>
              <CardHeader>
                <CardTitle>Product Basic Information</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <FormField
                    control={form.control}
                    name="name"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Product Name *</FormLabel>
                        <FormControl>
                          <Input {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="price"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Price *</FormLabel>
                        <FormControl>
                          <div className="relative">
                            <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground">
                              $
                            </span>
                            <Input
                              {...field}
                              type="number"
                              step="0.01"
                              min="0"
                              className="pl-8"
                              onChange={(e) =>
                                field.onChange(parseFloat(e.target.value) || 0)
                              }
                            />
                          </div>
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="quantity"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Quantity *</FormLabel>
                        <FormControl>
                          <Input
                            {...field}
                            type="number"
                            min="0"
                            onChange={(e) =>
                              field.onChange(parseInt(e.target.value) || 0)
                            }
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="status"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Status *</FormLabel>
                        <Select
                          onValueChange={field.onChange}
                          value={field.value}
                        >
                          <FormControl>
                            <SelectTrigger>
                              <SelectValue placeholder="Select status" />
                            </SelectTrigger>
                          </FormControl>
                          <SelectContent>
                            {PRODUCT_STATUSES.map((status) => (
                              <SelectItem key={status} value={status}>
                                {formatStatusLabel(status)}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="availableFrom"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Available From *</FormLabel>
                        <FormControl>
                          <Input {...field} type="datetime-local" />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="isActive"
                    render={({ field }) => (
                      <FormItem className="flex flex-row items-start space-x-3 space-y-0">
                        <FormControl>
                          <Checkbox
                            checked={field.value}
                            onCheckedChange={field.onChange}
                          />
                        </FormControl>
                        <div className="space-y-1 leading-none">
                          <FormLabel>Product is active</FormLabel>
                        </div>
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="brandId"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Brand *</FormLabel>
                        <Select
                          onValueChange={(value) =>
                            field.onChange(parseInt(value))
                          }
                          value={field.value.toString()}
                          disabled={isLoadingBrands || brands.length === 0}
                        >
                          <FormControl>
                            <SelectTrigger>
                              <SelectValue
                                placeholder={
                                  isLoadingBrands
                                    ? "Loading brands..."
                                    : "Select a brand"
                                }
                              />
                            </SelectTrigger>
                          </FormControl>
                          <SelectContent>
                            {brands.map((brand) => (
                              <SelectItem
                                key={brand.id}
                                value={brand.id.toString()}
                              >
                                {brand.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="categoryIds"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Categories *</FormLabel>
                        {isLoadingCategories ? (
                          <div className="flex items-center justify-center py-2">
                            <Loader2 className="h-4 w-4 animate-spin" />
                            <span className="ml-2 text-sm text-muted-foreground">
              Loading categories...
            </span>
                          </div>
                        ) : (
                          <Select value="placeholder" onValueChange={() => {}}>
                            <FormControl>
                              <SelectTrigger>
                                <SelectValue>
                                  {field.value && field.value.length > 0
                                    ? `${field.value.length} categories selected`
                                    : "Select categories"}
                                </SelectValue>
                              </SelectTrigger>
                            </FormControl>
                            <SelectContent>
                              {categories.map((category) => (
                                <label
                                  key={category.id}
                                  className="flex items-center space-x-2 cursor-pointer hover:bg-gray-50 p-1 rounded"
                                >
                                  <input
                                    type="checkbox"
                                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                                    checked={field.value.includes(category.id)}
                                    onChange={(e) => {
                                      if (e.target.checked) {
                                        field.onChange([
                                          ...field.value,
                                          category.id,
                                        ]);
                                      } else {
                                        field.onChange(
                                          field.value.filter(
                                            (id) => id !== category.id
                                          )
                                        );
                                      }
                                    }}
                                  />
                                  <span className="text-sm">{category.name}</span>
                                </label>
                               ))}
                             </SelectContent>
                           </Select>
                         )}
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

                <FormField
                  control={form.control}
                  name="image"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Product Image</FormLabel>
                      <FormControl>
                        <Input
                          type="file"
                          accept="image/*"
                          onChange={handleImageUpload}
                        />
                      </FormControl>
                      {imagePreview && (
                        <div className="mt-2">
                          <img
                            src={imagePreview}
                            alt="Preview"
                            className="h-32 w-32 object-cover rounded-md"
                          />
                        </div>
                      )}
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </CardContent>
            </Card>

            {/* Product Details Card */}
            <Card>
              <CardHeader>
                <CardTitle>Product Details</CardTitle>
              </CardHeader>
              <CardContent>
                <FormField
                  control={form.control}
                  name="details.description"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Description *</FormLabel>
                      <FormControl>
                        <Textarea {...field} rows={5} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </CardContent>
            </Card>

            {/* Product Attributes Card */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle>Product Attributes</CardTitle>
                <Button
                  type="button"
                  onClick={addAttribute}
                  variant="outline"
                  size="sm"
                >
                  <Plus className="mr-2 h-4 w-4" />
                  Add Attribute
                </Button>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {fields.map((field, index) => (
                    <div key={field.id} className="flex items-end space-x-2">
                      <FormField
                        control={form.control}
                        name={`attributes.${index}.name`}
                        render={({ field }) => (
                          <FormItem className="flex-1">
                            <FormLabel>Attribute Name</FormLabel>
                            <FormControl>
                              <Input
                                {...field}
                                placeholder="e.g., Color, Size"
                              />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={form.control}
                        name={`attributes.${index}.value`}
                        render={({ field }) => (
                          <FormItem className="flex-1">
                            <FormLabel>Attribute Value</FormLabel>
                            <FormControl>
                              <Input
                                {...field}
                                placeholder="e.g., Red, Large"
                              />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <Button
                        type="button"
                        onClick={() => removeAttribute(index)}
                        variant="outline"
                        size="sm"
                        className="text-destructive hover:text-destructive"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Form Actions */}
            <div className="flex justify-end space-x-2">
              <Button
                type="button"
                onClick={() => router.back()}
                variant="outline"
              >
                Cancel
              </Button>
              <Button type="submit" disabled={form.formState.isSubmitting}>
                {form.formState.isSubmitting && (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                )}
                Update Product
              </Button>
            </div>
          </form>
        </Form>
      </div>
    </AdminLayout>
  );
}

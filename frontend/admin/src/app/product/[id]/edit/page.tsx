"use client";

import { catalogApi } from "@/api/index.api";
import AdminLayout from "@/components/layout/AdminLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
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
import { PRODUCT_STOCK_STATUSES } from "@/constants";
import { withRequireAdmin } from "@/hoc/withAuth";

import {
  Brand,
  Category,
  Product,
  UpdateProductRequest,
} from "@/interfaces/product.interface";
import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowLeft, Loader2, Plus, Trash2 } from "lucide-react";
import Image from "next/image";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState, useRef } from "react";
import { useFieldArray, useForm, ControllerRenderProps } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

const productSchema = z.object({
  id: z.string().min(1, "Product ID is required"),
  name: z.string().min(1, "Product name is required"),
  price: z.number().min(0, "Price must be greater than or equal to 0"),
  stockStatus: z.enum(PRODUCT_STOCK_STATUSES),
  quantity: z.number().min(0, "Quantity must be greater than or equal to 0"),
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

function AdminProductEditPage() {
  const router = useRouter();
  const params = useParams();
  const productIdParam = params.id;
  const productId = Array.isArray(productIdParam) ? productIdParam[0] : productIdParam;
  const hasInitializedStaticData = useRef(false);
  const [isLoadingProduct, setIsLoadingProduct] = useState(true);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [product, setProduct] = useState<Product | null>(null);

  // Brands state
  const [brands, setBrands] = useState<Brand[]>([]);
  const [isLoadingBrands, setIsLoadingBrands] = useState(true);

  // Categories state
  const [categories, setCategories] = useState<Category[]>([]);
  const [isLoadingCategories, setIsLoadingCategories] = useState(true);

  const form = useForm<ProductFormData>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      id: "",
      name: "",
      price: 0,
      stockStatus: "IN_STOCK",
      quantity: 0,
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

  // Load static data and product on component mount
  useEffect(() => {
    if (hasInitializedStaticData.current) {
      return;
    }

    hasInitializedStaticData.current = true;

    const loadBrands = async () => {
      try {
        setIsLoadingBrands(true);
        const brandsResponse = await catalogApi.retrieveAllBrand();

        if (!brandsResponse.success) {
          throw new Error(brandsResponse.errorMessage || "Failed to load brands");
        }

        setBrands(brandsResponse.data || []);
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to load brands";
        toast.error(errorMessage);
      } finally {
        setIsLoadingBrands(false);
      }
    };

    const loadCategories = async () => {
      try {
        setIsLoadingCategories(true);
        const categoriesResponse = await catalogApi.retrieveAllCategory();

        if (!categoriesResponse.success) {
          throw new Error(
            categoriesResponse.errorMessage || "Failed to load categories"
          );
        }

        setCategories(categoriesResponse.data || []);
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to load categories";
        toast.error(errorMessage);
      } finally {
        setIsLoadingCategories(false);
      }
    };
    
    const initializeData = async () => {
      await Promise.all([loadBrands(), loadCategories()]);
    };
    
    initializeData();
  }, []);

  useEffect(() => {
    if (!productId || typeof productId !== "string") {
      setIsLoadingProduct(false);
      setProduct(null);
      return;
    }

    const loadProduct = async () => {
      setIsLoadingProduct(true);
      try {
        const productResponse = await catalogApi.retrieveProduct(productId);

        if (productResponse.success) {
          const productData = productResponse.data;
          setProduct(productData);

          const availableFromDate = new Date(productData.availableFrom || new Date());
          const formattedDate = availableFromDate.toISOString().slice(0, 16);

          form.reset({
            id: productData.id,
            name: productData.name,
            price: productData.price,
            stockStatus: productData.stockStatus,
            quantity: productData.quantity,
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

          if (productData.image) {
            setImagePreview(productData.image);
          }

          replace(productData.attributes || []);
        } else {
          toast.error("Product not found");
          router.push("/admin/product");
        }
      } catch {
        toast.error("Failed to load product data");
        router.push("/admin/product");
      } finally {
        setIsLoadingProduct(false);
      }
    };

    loadProduct();
  }, [form, productId, replace, router]);

  const handleImageUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setImageFile(file);
      const reader = new FileReader();
      reader.onload = (e) => {
        const result = e.target?.result as string;
        setImagePreview(result);
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
        .map((attr) => ({
          id: attr.id || undefined, // Use existing ID or 0 for new attributes
          name: attr.name,
          value: attr.value,
        }));

      const formData: UpdateProductRequest = {
        ...data,
        attributes: filteredAttributes,
        availableFrom: new Date(data.availableFrom).toISOString(),
      };

      const response = await catalogApi.updateProduct(formData, imageFile ?? undefined);

      if (response.success) {
        toast.success("Product updated successfully!");
        router.push(`/admin/product/${formData.id}`);
      } else {
        toast.error(response.errorMessage || "Failed to update product");
      }
    } catch {
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
              The product you&apos;re looking for doesn&apos;t exist.
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
                    render={({ field }: { field: ControllerRenderProps<ProductFormData, "name"> }) => (
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
                    render={({ field }: { field: ControllerRenderProps<ProductFormData, "price"> }) => (
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
                              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
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
                    render={({ field }: { field: ControllerRenderProps<ProductFormData, "quantity"> }) => (
                      <FormItem>
                        <FormLabel>Quantity *</FormLabel>
                        <FormControl>
                          <Input
                            {...field}
                            type="number"
                            min="0"
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
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
                    name="stockStatus"
                    render={({ field }: { field: ControllerRenderProps<ProductFormData, "stockStatus"> }) => (
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
                            {PRODUCT_STOCK_STATUSES.map((status) => (
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
                    render={({ field }: { field: ControllerRenderProps<ProductFormData, "availableFrom"> }) => (
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
                    render={({ field }: { field: ControllerRenderProps<ProductFormData, "isActive"> }) => (
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
                    render={({ field }: { field: ControllerRenderProps<ProductFormData, "brandId"> }) => (
                      <FormItem>
                        <FormLabel>Brand *</FormLabel>
                        <Select
                          onValueChange={(value: string) =>
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
                    render={({ field }: { field: ControllerRenderProps<ProductFormData, "categoryIds"> }) => (
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
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button
                                variant="outline"
                                className="w-full justify-between font-normal"
                                disabled={!categories.length}
                              >
                                {field.value && field.value.length > 0
                                  ? `${field.value.length} categories selected`
                                  : "Select categories"}
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent className="w-64">
                              {categories.map((category) => {
                                const isChecked = field.value.includes(category.id);
                                return (
                                  <DropdownMenuCheckboxItem
                                    key={category.id}
                                    checked={isChecked}
                                    onCheckedChange={(checked) => {
                                      if (checked) {
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
                                  >
                                    {category.name}
                                  </DropdownMenuCheckboxItem>
                                );
                              })}
                            </DropdownMenuContent>
                          </DropdownMenu>
                        )}
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

                <FormField
                  control={form.control}
                  name="image"
                  render={() => (
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
                          <Image
                            src={imagePreview}
                            alt="Preview"
                            width={128}
                            height={128}
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
                  render={({ field }: { field: ControllerRenderProps<ProductFormData, "details.description"> }) => (
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
                        render={({ field }: { field: ControllerRenderProps<ProductFormData, `attributes.${number}.name`> }) => (
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
                        render={({ field }: { field: ControllerRenderProps<ProductFormData, `attributes.${number}.value`> }) => (
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

export default withRequireAdmin(AdminProductEditPage);

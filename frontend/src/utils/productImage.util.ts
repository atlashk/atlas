export const getProductImageUrl = (
  image: string | null | undefined,
): string => {
  if (!image) {
    // Handle null/undefined/empty case - use Next.js public directory
    return "/product-placeholder.jpg";
  }

  // Handle both URL and base64 cases - they can be used directly in src
  return image;
};

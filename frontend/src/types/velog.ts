export type VelogPost = {
  id: string;
  title: string;
  short_description: string;
  url_slug: string;
  tags: string[];
  released_at: string;
};

export type VelogPostsResponse = {
  data?: {
    posts?: VelogPost[];
  };
  errors?: Array<{
    message: string;
  }>;
};

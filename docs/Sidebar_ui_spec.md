# Sidebar.tsx — UI Extraction & Android Implementation Guide

**Generated:** Sidebar UI extraction and Android implementation suggestions.

**Source file:** `/mnt/data/Sidebar.tsx`


----


## 1) Raw file content (shortened)

```tsx
import React from "react";
import { NavLink } from "react-router-dom";
import { motion, AnimatePresence, Variants } from "framer-motion";
import {
  FilePlusIcon,
  HistoryIcon,
  LandmarkIcon,
  UserPlusIcon,
  UsersIcon,
  BookOpenIcon,
  StarIcon,
  HomeIcon,
} from "../constants";
import { useData } from "../context/DataContext";
import HamburgerIcon from "./ui/HamburgerIcon";
import { ProfileHeaderHandle } from "./ProfileHeader";

const DatabaseIcon = (props: React.SVGProps<SVGSVGElement>) => (
  <svg
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth={1.5}
    strokeLinecap="round"
    strokeLinejoin="round"
    {...props}
  >
    <ellipse cx="12" cy="6" rx="8" ry="3" />
    <path d="M4 6v6c0 1.657 3.582 3 8 3s8-1.343 8-3V6" />
    <path d="M4 12v6c0 1.657 3.582 3 8 3s8-1.343 8-3v-6" />
  </svg>
);

// Animation variants
const menuDropdownVariants: Variants = {
  hidden: {
    opacity: 0,
    x: -10,
    scale: 0.95,
  },
  visible: {
    opacity: 1,
    x: 0,
    scale: 1,
    transition: {
      type: 'spring',
      stiffness: 300,
      damping: 25,
      staggerChildren: 0.05,
    },
  },
  exit: {
    opacity: 0,
    x: -10,
    scale: 0.95,
    transition: { duration: 0.15 },
  },
};

const menuItemVariants: Variants = {
  hidden: { opacity: 0, x: -10 },
  visible: {
    opacity: 1,
    x: 0,
    transition: {
      type: 'spring',
      stiffness: 300,
      damping: 24,
    },
  },
};

const navItemVariants: Variants = {
  idle: { scale: 1, x: 0 },
  hover: {
    scale: 1.02,
    x: 4,
    transition: {
      type: 'spring',
      stiffness: 400,
      damping: 20,
    },
  },
  tap: { scale: 0.98 },
};

const iconVariants: Variants = {
  idle: { scale: 1, rotate: 0 },
  hover: {
    scale: 1.1,
    rotate: [0, -5, 5, 0],
    transition: {
      scale: { type: 'spring', stiffness: 400, damping: 20 },
      rotate: { duration: 0.3 },
    },
  },
};

const profileButtonVariants: Variants = {
  idle: { scale: 1 },
  hover: {
    scale: 1.1,
    boxShadow: '0 4px 12px rgba(99, 102, 241, 0.3)',
    transition: {
      type: 'spring',
      stiffness: 400,
      damping: 20,
    },
  },
  tap: { scale: 0.95 },
};

const allNavItems = [
  { path: "/", label: "Add Customer", icon: UserPlusIcon, adminOnly: true },
  { path: "/add-record", label: "Add Record", icon: FilePlusIcon, adminOnly: true },
  { path: "/customers", label: "Customers", icon: UsersIcon, adminOnly: true },
  { path: "/loans", label: "Loans", icon: LandmarkIcon },
  { path: "/loan-seniority", label: "Loan Seniority", icon: StarIcon },
  { path: "/subscriptions", label: "Subscriptions", icon: HistoryIcon },
  { path: "/data", label: "Expenditure", icon: DatabaseIcon },
  { path: "/summary", label: "Summary", icon: BookOpenIcon },
];

interface SidebarProps {
  profileRef: React.RefObject<ProfileHeaderHandle>;
}

const Sidebar: React.FC<SidebarProps> = ({ profileRef }) => {
  const {
    session,
    signOut,
    isScopedCustomer,
    scopedCustomerId,
    customers = [],
    customerMap,
  } = useData();

  const [showLandscapeMenu, setShowLandscapeMenu] = React.useState(false);
  const menuRef = React.useRef<HTMLDivElement>(null);

  let navItems = allNavItems.filter((item) => !item.adminOnly || !isScopedCustomer);

  // For scoped customers, add a Home link that navigates to the customer dashboard above Loans
  if (isScopedCustomer) {
    const homeItem = { path: '/', label: 'Home', icon: HomeIcon };
    const loansIndex = navItems.findIndex((it) => it.path === '/loans');
    if (loansIndex >= 0) {
      navItems.splice(loansIndex, 0, homeItem);
    } else {
      navItems.unshift(homeItem);
    }
  }
  const activeLinkClass = "bg-indigo-50 text-indigo-600 font-semibold dark:bg-indigo-900/30 dark:text-indigo-400";
  const inactiveLinkClass = "text-gray-600 hover:bg-gray-100 hover:text-gray-900 dark:text-dark-muted dark:hover:bg-slate-700 dark:hover:text-dark-text";

  const [collapsed, setCollapsed] = React.useState(true);

  // Close landscape dropdown when clicking outside
  React.useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setShowLandscapeMenu(false);
      }
    };
    if (showLandscapeMenu) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [showLandscapeMenu]);

  // --- RESPONSIVE OFFSET LOGIC ---
  React.useEffect(() => {
    const applyVar = () => {
      if (typeof window === "undefined") return;

      const isLargeDesktop = window.matchMedia("(min-width: 1024px)").matches;
      const isTabletPortrait = window.matchMedia("(min-width: 640px) and (orientation: portrait)").matches;
      const isMobileLandscape = window.matchMedia("(max-width: 1023px) and (orientation: landscape)").matches;

      // Common visual constants
      const leftOffset = 16;
      const gap = 16;

      let total = "0px";

      if (isLargeDesktop || isTabletPortrait) {
        const sidebarWidth = collapsed ? 80 : 256;
        total = `${sidebarWidth + leftOffset + gap}px`;
      }
      // Mobile Landscape
      else if (isMobileLandscape) {
        const sidebarWidth = 96;
        total = `${sidebarWidth + leftOffset + gap}px`;
      }

      try {
        document.documentElement.style.setProperty("--sidebar-offset", total);
      } catch (e) { }
... (file continues)```


----


## 2) Components / Exports found

_No named components found via simple regex. The file may export default a component._


## 3) JSX tags and UI building blocks used

AnimatePresence, HTMLDivElement, HamburgerIcon, NavLink, ProfileHeaderHandle, SVGSVGElement, SidebarProps, div, div, ellipse, nav, number, p, path, span, svg


## 4) CSS classes / tailwind-like classes observed

Found `className` values (likely Tailwind CSS or CSS modules):


- `h-28 sm:hidden landscape:hidden`

- `fixed bottom-0 left-0 right-0 z-30 bg-white border-t border-gray-200 sm:hidden landscape:hidden dark:bg-dark-card dark:border-dark-border`

- `flex justify-around items-center py-2 overflow-x-auto w-full gap-1`

- `flex flex-col items-center`

- `w-6 h-6 flex-shrink-0`

- `mt-1 text-center leading-tight`

- `flex flex-col items-center justify-center px-2 py-1.5 min-w-[60px] max-w-[80px] flex-shrink-0 transition-colors duration-200 text-[10px] text-gray-500 hover:text-gray-700 dark:text-dark-muted dark:hover:text-dark-text`

- `w-6 h-6 flex-shrink-0 rounded-full bg-indigo-600 text-white font-semibold flex items-center justify-center text-xs`

- `fixed top-4 bottom-4 left-4 z-50 w-[96px] bg-white rounded-2xl border border-gray-200 shadow-sm hidden landscape:flex lg:landscape:hidden flex-col justify-between items-center py-4 dark:bg-dark-card dark:border-dark-border`

- `relative flex justify-center w-full`

- `w-7 h-7`

- `absolute top-0 left-full ml-4 w-96 bg-white rounded-xl shadow-xl border border-gray-100 overflow-hidden py-1 z-50 dark:bg-dark-card dark:border-dark-border`

- `px-3 py-2 border-b border-gray-100 bg-gray-50/50 dark:border-dark-border dark:bg-slate-800/50`

- `text-xs font-semibold text-gray-500 uppercase tracking-wider dark:text-dark-muted`

- `max-h-[85vh] overflow-y-auto p-2 grid grid-cols-2 gap-2`

- `w-4 h-4 mr-2 shrink-0`

- `truncate`

- `fixed left-4 top-4 bottom-4 z-40 bg-white rounded-2xl border border-gray-200 shadow-sm flex flex-col hidden sm:flex landscape:hidden lg:landscape:flex dark:bg-dark-card dark:border-dark-border`

- `text-xl lg:text-2xl font-bold text-gray-800 truncate dark:text-dark-text`

- `hidden lg:inline`

- `lg:hidden`

- `p-2 rounded-md hover:bg-gray-100 shrink-0 dark:hover:bg-slate-700`

- `w-5 h-5 text-gray-700 dark:text-dark-muted`

- `flex-1 flex flex-col min-h-0`

- `flex-1 p-4 space-y-2 overflow-y-auto min-h-0`

- `relative flex items-center w-full`

- `w-6 h-6 text-current`

- `inline-block overflow-hidden whitespace-nowrap ml-3`

- `absolute left-full ml-2 top-1/2 -translate-y-1/2 pointer-events-none hidden group-hover:block`

- `bg-gray-800 text-white text-xs rounded px-2 py-1 whitespace-nowrap dark:bg-slate-600`

- `shrink-0`

- `p-4 border-t border-gray-200 text-center text-xs text-gray-400 dark:border-dark-border dark:text-dark-muted`



## 5) Icons & graphic components

Possible icon/graphic components used (component names starting with capital letter):


- `NavLink`

- `HamburgerIcon`



## 6) Visible text / labels found (candidate menu items, headings)

- `;
}

const Sidebar: React.FC`

- `{item.label}`

- `{initials}`

- `Profile`

- `{/* Top: Hamburger */}`

- `Navigate`

- `Loan Management`

- `Loans`



## 7) React hooks / local state (heuristic)

_No `useState`/`useEffect` matches found by simple regex._



## 8) Imports (used libraries & modules)

- `React` from `react`

- `{ NavLink }` from `react-router-dom`

- `{ motion, AnimatePresence, Variants }` from `framer-motion`

- `{ useData }` from `../context/DataContext`

- `HamburgerIcon` from `./ui/HamburgerIcon`

- `{ ProfileHeaderHandle }` from `./ProfileHeader`


## 9) Dynamic class composition helpers

_No `cn(...)` calls found._


## 10) Navigation elements / links

- Uses link-like elements: NavLink


----


## 11) Android mapping & implementation checklist


Below are the UI elements, behaviors and assets your AI agent will need to implement in the Android app (Jetpack Compose suggested). Where possible, I've mapped Tailwind/CSS semantics to Compose equivalents and XML suggestions.


### Colors & visual tokens (from class names)

- `bg-gray-50/50` (Tailwind token; map to Android color resource)

- `bg-gray-800` (Tailwind token; map to Android color resource)

- `bg-indigo-600` (Tailwind token; map to Android color resource)

- `bg-white` (Tailwind token; map to Android color resource)

- `border-b` (Tailwind token; map to Android color resource)

- `border-gray-100` (Tailwind token; map to Android color resource)

- `border-gray-200` (Tailwind token; map to Android color resource)

- `border-t` (Tailwind token; map to Android color resource)

- `text-[10px]` (Tailwind token; map to Android color resource)

- `text-center` (Tailwind token; map to Android color resource)

- `text-current` (Tailwind token; map to Android color resource)

- `text-gray-400` (Tailwind token; map to Android color resource)

- `text-gray-500` (Tailwind token; map to Android color resource)

- `text-gray-700` (Tailwind token; map to Android color resource)

- `text-gray-800` (Tailwind token; map to Android color resource)

- `text-white` (Tailwind token; map to Android color resource)

- `text-xl` (Tailwind token; map to Android color resource)

- `text-xs` (Tailwind token; map to Android color resource)



### Layout & containers

- Sidebar container: vertical column, full height, scrollable when content overflows. Use `Column` + `verticalScroll()` or `Drawer`/`ModalDrawer` for side navigation.

- Sections: group headings, dividers, collapsed/expandable groups.

- Each nav item: left icon, label text, optional trailing badge/counter, padding, selectable/active state.


### Controls & interactive behaviors

- Click handlers on nav items to navigate. Map to `NavController.navigate()`.

- Active/selected state: different background or text color when selected.

- Collapsible groups (accordion): maintain expanded boolean per group.

- Hover states (web): ignore for mobile or replace with touch ripple.


### Icons & avatars

- Replace React icon components with vector drawable (SVG) or use `ImageVector` icons in Compose. Export needed icons from project or use Material/Feather equivalents.

- Avatar: circular image with fallback initials. Use `Image` with `clip(CircleShape)` and `contentScale = Crop`.


### Typography

- Headings, menu labels and small helper text exist. Map font sizes to `sp` values and keep consistent scale (e.g., 16sp for labels, 14sp for small helper text).


### Dividers / separators

- Use `Divider` with 1dp height and subtle color.


### Accessibility

- Provide `contentDescription` for icons and images.

- Ensure touch targets >= 48dp.


### Suggested Jetpack Compose snippets (skeletons)

```kotlin
@Composable
fun Sidebar(navItems: List<NavItem>, onNavigate: (String)->Unit) {
  Column(modifier = Modifier
    .fillMaxHeight()
    .width(280.dp)
    .verticalScroll(rememberScrollState())
    .background(MaterialTheme.colors.surface)) {

    // Header / avatar
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Image(
         painter = rememberImagePainter(data = avatarUrl),
         contentDescription = "User avatar",
         modifier = Modifier.size(40.dp).clip(CircleShape)
      )
      Spacer(Modifier.width(12.dp))
      Column { Text("User Name", style = MaterialTheme.typography.subtitle1) }
    }

    Divider()

    navItems.forEach { item ->
      SidebarItem(item = item, onClick = { onNavigate(item.route) })
    }
  }
}
```


## 12) Actionable checklist for AI agent

- [ ] Extract exact color hex values from the project's Tailwind config or CSS files and create Android `colors.xml` entries.

- [ ] Export used SVG icons and convert to Android Vector Drawables (or use Compose ImageVector).

- [ ] Create `NavItem` data models (id, label, icon, route, badgeCount, isGroup, children).

- [ ] Implement `Sidebar`, `SidebarItem`, `SidebarGroup` (collapsible) in Jetpack Compose. Add visual states (selected, disabled).

- [ ] Implement Avatar component with image+initials fallback and accessibility labels.

- [ ] Map CSS spacing/tokens to dp/sp scale and create a design token file in Android.

- [ ] Add tests/stories (if using Compose Preview) to preview selected vs. unselected states.


----


## 13) Notes & limitations

- This extraction is heuristic — it's based on static regex parsing of the TSX file. For precise color hex codes, exact typography tokens, and image assets, the AI agent should inspect the project's Tailwind config, global CSS, and `public`/`assets` folders.

- If the TSX uses dynamic runtime values (theme, user data), implement corresponding state handling in Android (ViewModel + StateFlows).

